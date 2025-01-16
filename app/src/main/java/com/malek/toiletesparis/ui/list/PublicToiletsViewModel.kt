package com.malek.toiletesparis.ui.list

import androidx.lifecycle.ViewModel
import com.malek.toiletesparis.domain.GetPublicToiletsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PublicToiletsViewModel @Inject constructor(
    private val useCase: GetPublicToiletsUseCase
) : ViewModel() {

}