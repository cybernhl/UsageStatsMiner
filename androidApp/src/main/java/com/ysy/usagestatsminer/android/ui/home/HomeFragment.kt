package com.ysy.usagestatsminer.android.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.drake.brv.utils.linear
import com.drake.brv.utils.models
import com.drake.brv.utils.setup
import com.ysy.usagestatsminer.android.R
import com.ysy.usagestatsminer.android.databinding.FragmentHomeBinding
import java.util.concurrent.TimeUnit

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.rvUsageEvents.apply {
            this.linear().setup {
                addType<String>(R.layout.item_rv_usage_events)
                onBind {
                    with(getModel<String>()) {
                        findView<TextView>(R.id.tv_usage_event).text = this
                    }
                }
            }
        }
        homeViewModel.usageEventsLD.observe(viewLifecycleOwner) {
            binding.rvUsageEvents.models = it
        }
        homeViewModel.queryUsageEventsFromSystem(
            System.currentTimeMillis() - TimeUnit.HOURS.toMillis(12),
            System.currentTimeMillis()
        )

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
