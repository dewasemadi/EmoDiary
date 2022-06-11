package com.dicoding.emodiary.ui.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.viewModels
import com.dicoding.emodiary.data.remote.body.UpdateProfileBody
import com.dicoding.emodiary.data.remote.response.BaseResponse
import com.dicoding.emodiary.data.remote.response.UserItem
import com.dicoding.emodiary.databinding.FragmentEditProfileBinding
import com.dicoding.emodiary.ui.viewmodel.MainViewModel
import com.dicoding.emodiary.ui.viewmodel.ViewModelFactory
import com.dicoding.emodiary.utils.*
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class ModalBottomSheet : BottomSheetDialogFragment() {
    private var _binding: FragmentEditProfileBinding? = null
    private lateinit var session: SessionManager
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    companion object {
        const val TAG = "ModalBottomSheet"
        const val FLAG_ID = "extra_flag"
    }

    val viewModel: MainViewModel by viewModels {
        ViewModelFactory.getInstance(requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root
        session = SessionManager(requireActivity())
        binding.edtData.requestFocus()
        val inputManager =
            context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.showSoftInput(binding.edtData, InputMethodManager.SHOW_IMPLICIT)
//        setupView()
//        setupAction()
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val flagId = arguments?.getInt(FLAG_ID)

        if(flagId == 0){
            binding.tvTitle.text = "Masukkan nama anda"
            binding.tvReTitle.visibility = View.GONE
            binding.edtReData.visibility = View.GONE
            binding.edtData.setText(session.getString(FULL_NAME))
        }else if(flagId == 1){
            binding.tvTitle.text = "Masukkan email anda"
            binding.tvReTitle.visibility = View.GONE
            binding.edtReData.visibility = View.GONE
            binding.edtData.setText(session.getString(EMAIL))
        }else if(flagId == 2){
            binding.tvTitle.text = "Masukkan password lama anda"
            binding.tvReTitle.visibility = View.VISIBLE
            binding.edtReData.visibility = View.VISIBLE
        }

        binding.btnSave.setOnClickListener {
            val current = binding.edtData.text.toString()
            val newPassword = binding.edtReData.text.toString()
            if(flagId == 0 && current.isNotEmpty()){
                val data = UpdateProfileBody()
                updateData(data)
            }else if(flagId == 1 && current.isNotEmpty()){
                val data = UpdateProfileBody(email = current)
                updateData(data)
            }else if(flagId == 2 && current.isNotEmpty() && newPassword.isNotEmpty()){
                val data = UpdateProfileBody(currentPassword = current, newPassword = newPassword)
                updateData(data)
            }
        }

    }

    private fun updateData(newData: UpdateProfileBody) {
        viewModel.updateProfile(session.getString(USER_ID), newData).observe(this){
            when (it){
                is State.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSave.visibility = View.INVISIBLE
                }
                is State.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.visibility = View.VISIBLE
                    it.data.data?.let { res -> setToSession(res) }
                    Toast.makeText(requireActivity(), "Data berhasil disimpan", Toast.LENGTH_SHORT).show()
                }
                is State.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.visibility = View.VISIBLE
                    Toast.makeText(requireActivity(), "Data gagal disimpan", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setToSession(data: UserItem) {
        val flagId = arguments?.getInt(FLAG_ID)
        if(flagId == 0){
            data.fullname?.let { session.setString(FULL_NAME, it) }
        }else if(flagId == 1 ){
            data.email?.let { session.setString(EMAIL, it) }
        }
    }


    private fun View.showKeyboard() {
        val inputManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.showSoftInput(this, InputMethodManager.SHOW_FORCED)
    }

    private fun View.hideKeyboard() {
        val inputManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(windowToken, 0)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}