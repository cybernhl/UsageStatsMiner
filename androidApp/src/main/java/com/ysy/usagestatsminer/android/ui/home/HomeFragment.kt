package com.ysy.usagestatsminer.android.ui.home

import android.content.DialogInterface
import android.content.DialogInterface.OnMultiChoiceClickListener
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.blankj.utilcode.util.AppUtils
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.ysy.usagestatsminer.android.R
import com.ysy.usagestatsminer.android.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel

    private val appIconCache = mutableMapOf<String, Drawable?>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.rvUsageEvents.apply {
            this.linear().setup {
                addType<HomeListItem>(R.layout.item_rv_usage_events)
                onBind {
                    getModel<HomeListItem>().let {
                        findView<TextView>(R.id.tv_usage_event).text = it.text
                        findView<ImageView>(R.id.iv_usage_event).setImageDrawable(
                            appIconCache[it.pkgName] ?: AppUtils.getAppIcon(it.pkgName).also { d ->
                                appIconCache[it.pkgName] = d
                            }
                        )
                    }
                }
            }
        }
        viewModel.usageEventsLD.observe(viewLifecycleOwner) {
            binding.rvUsageEvents.models = it
        }

        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.home_fragment_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                when (menuItem.itemId) {
                    R.id.action_filter_app -> showAppFilter()
                    R.id.action_filter_event -> showEventFilter()
                }
                return true
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        updateData()
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateData() {
        viewModel.queryUsageEventsFromSystem(
            System.currentTimeMillis() - TimeUnit.HOURS.toMillis(3),
            System.currentTimeMillis()
        )
    }

    private fun showAppFilter() = lifecycleScope.launch {
        val pkgs = withContext(Dispatchers.IO) { viewModel.appPkgs.toTypedArray() }
        val checkedItems = pkgs.map { s ->
            viewModel.filterPkgs.getOrDefault(s, true)
        }.toBooleanArray()
        AlertDialog.Builder(requireContext())
            .setTitle("App Filter")
            .setMultiChoiceItems(
                viewModel.appNames.toTypedArray(),
                checkedItems
            ) { _, which, isChecked ->
                viewModel.filterPkgs[pkgs[which]] = isChecked
            }
            .setPositiveButton("OK") { _, _ ->
                updateData()
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }

    private fun showEventFilter() {
        val checkedItems = viewModel.eventDescs.mapIndexed { index, _ ->
            viewModel.filterEventTypes.getOrDefault(index, true)
        }.toBooleanArray()
        AlertDialog.Builder(requireContext())
            .setTitle("Event Filter")
            .setMultiChoiceItems(viewModel.eventDescs, checkedItems) { _, which, isChecked ->
                viewModel.filterEventTypes[which] = isChecked
            }
            .setPositiveButton("OK") { _, _ ->
                updateData()
            }
            .setNegativeButton("CANCEL", null)
            .show()
    }
}
