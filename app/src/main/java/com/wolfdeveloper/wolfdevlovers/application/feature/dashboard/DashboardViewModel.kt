package com.wolfdeveloper.wolfdevlovers.application.feature.dashboard

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wolfdeveloper.wolfdevlovers.application.domain.model.Lover
import com.wolfdeveloper.wolfdevlovers.application.domain.model.User
import com.wolfdeveloper.wolfdevlovers.application.feature.main.MainViewModel.Navigation
import com.wolfdeveloper.wolfdevlovers.application.feature.dashboard.DashboardViewModel.ScreenEvent
import com.wolfdeveloper.wolfdevlovers.application.usecase.DashboardBackgroundUseCase
import com.wolfdeveloper.wolfdevlovers.application.usecase.DashboardImageLoverUseCase
import com.wolfdeveloper.wolfdevlovers.application.usecase.DashboardInsertUseCase
import com.wolfdeveloper.wolfdevlovers.application.usecase.DashboardProfileUseCase
import com.wolfdeveloper.wolfdevlovers.commons.extensions.isNotNull
import com.wolfdeveloper.wolfdevlovers.commons.extensions.isNull
import com.wolfdeveloper.wolfdevlovers.commons.extensions.Result
import com.wolfdeveloper.wolfdevlovers.commons.extensions.isZero
import com.wolfdeveloper.wolfdevlovers.commons.extensions.EMPTY_STRING
import com.wolfdeveloper.wolfdevlovers.commons.viewModel.ChannelEventSenderImpl
import com.wolfdeveloper.wolfdevlovers.commons.viewModel.EventSender
import com.wolfdeveloper.wolfdevlovers.coreapi.session.ISessioInput
import com.wolfdeveloper.wolfdevlovers.coreapi.session.ISessionOutput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import com.wolfdeveloper.wolfdevlovers.R

class DashboardViewModel(
    private val dashboardInsertUseCase: DashboardInsertUseCase,
    private val dashboardBackgroundUseCase: DashboardBackgroundUseCase,
    private val dashboardImageLoverUseCase: DashboardImageLoverUseCase,
    private val dashboardProfileUseCase: DashboardProfileUseCase,
    private val input: ISessioInput,
    private val output: ISessionOutput,
) : ViewModel(), EventSender<ScreenEvent> by ChannelEventSenderImpl() {

    val uiState = UiState()

    fun setup() {
        output.getCode().let {
            if (it.isNotEmpty()) {
                uiState.code.value = it
            }
        }
    }

    fun insert(activity: Activity) = uiState.run {

        if (name.value.isEmpty()) {
            invalideToast(activity = activity, id = R.string.invalide_name)
            return@run
        }
        if (nameLover.value.isEmpty()) {
            invalideToast(activity = activity, id = R.string.invalide_his_name)
            return@run
        }
        if (this@run.profileImage.value.isNull()) {
            invalideToast(activity = activity, id = R.string.invalide_profile)
            return@run
        }
        if (this@run.backgroundImage.value.isNull()) {
            invalideToast(activity = activity, id = R.string.invalide_background)
            return@run
        }
        if (this@run.lovers.filter {
                it.Image.value.isNotNull() && it.text.value.isNotEmpty()
            }.isEmpty()) {
            invalideToast(activity = activity, id = R.string.invalide_lovers)
            return@run
        }

        viewModelScope.launch {
            screenState.value = ScreenState.ScreenLoading
            when (val result = dashboardInsertUseCase.execute(
                User().apply {
                    myName = this@run.name.value
                    nameLover = this@run.nameLover.value
                    plus = this@run.plus.value
                    spotify = this@run.spotify.value
                    whatssap = this@run.whatssap.value
                    instagram = this@run.instagram.value
                    lovers = this@run.lovers.filter {
                        !it.text.value.isBlank() && it.Image.value.isNotNull()
                    }.map {
                        Lover().apply {
                            textLover = it.text.value
                            music = it.music.value
                        }
                    }
                }
            )) {
                is Result.Success -> {
                    if (result.data.id.isZero()) {
                        screenState.value = ScreenState.ScreenContent
                        invalideToast(activity = activity, result.data.code)
                    }

                    this@run.code.value = result.data.code

                    this@run.profileImage.value?.let {
                        dashboardProfileUseCase.execute(
                            uri = it,
                            code = this@run.code.value
                        )
                    }

                    this@run.backgroundImage.value?.let {
                        dashboardBackgroundUseCase.execute(
                            uri = it,
                            code = this@run.code.value
                        )
                    }

                    if (result.data.lovers.isNotEmpty()) {
                        this@run.lovers.filter {
                            it.Image.value.isNotNull()
                        }.forEachIndexed { index, it ->
                            it.Image.value?.let {
                                dashboardImageLoverUseCase.execute(
                                    uri = it,
                                    id = result.data.lovers[index].id
                                )
                            }
                        }
                    }

                    screenState.value = ScreenState.ScreenSuccess
                }
                is Result.Failure -> {
                    screenState.value = ScreenState.ScreenContent
                    invalideToast(activity = activity, result.err.message.orEmpty())
                }
            }
        }
    }

    private fun invalideToast(activity: Activity, id: Int) = activity.run {
        Toast.makeText(this, getString(id), Toast.LENGTH_LONG).show();
    }

    private fun invalideToast(activity: Activity, text: String) = activity.run {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    fun onClickInsert() = viewModelScope.launch {
        uiState.screenState.value = ScreenState.ScreenContent
    }

    fun onClickDashboard() = viewModelScope.launch {
        uiState.screenState.value = ScreenState.ScreenDashBoard
    }

    fun onClickAccess() = uiState.code.value.run {
        if (this.isNotEmpty()) {
            input.setCode(this)
            input.setUser(null)
            onClickMenu()
        }
    }

    fun onClickMenu() = viewModelScope.launch {
        sendEvent(ScreenEvent.NavigateTo(Navigation.MenuPopStack))
    }

    fun onClickBackSpace() = uiState.screenState.run {
        if (value != ScreenState.ScreenDashBoard) {
            onClickDashboard()
        }
    }

    sealed class ScreenState {
        object ScreenDashBoard : ScreenState()
        object ScreenLoading : ScreenState()
        object ScreenContent : ScreenState()
        object ScreenSuccess : ScreenState()
        object ScreenError : ScreenState()
    }

    sealed class ScreenEvent {
        object GoBack : ScreenEvent()
        data class NavigateTo(val navigation: Navigation) : ScreenEvent()
    }

    class UiState {
        private val RANGE_LOVERS = 0..4

        val screenState = MutableStateFlow<ScreenState>(ScreenState.ScreenDashBoard)

        val code = mutableStateOf(EMPTY_STRING)

        val name = mutableStateOf(EMPTY_STRING)
        val nameLover = mutableStateOf(EMPTY_STRING)
        val plus = mutableStateOf(EMPTY_STRING)
        val spotify = mutableStateOf(EMPTY_STRING)
        val instagram = mutableStateOf(EMPTY_STRING)
        val whatssap = mutableStateOf(EMPTY_STRING)

        val profileImage = MutableStateFlow<Uri?>(null)
        val backgroundImage = MutableStateFlow<Uri?>(null)

        val lovers = mutableStateListOf<LoverState>().apply {
            for (i in RANGE_LOVERS) {
                add(LoverState())
            }
        }

        class LoverState {
            val text = mutableStateOf(EMPTY_STRING)
            val music = mutableStateOf(EMPTY_STRING)
            val Image = MutableStateFlow<Uri?>(null)
        }
    }
}