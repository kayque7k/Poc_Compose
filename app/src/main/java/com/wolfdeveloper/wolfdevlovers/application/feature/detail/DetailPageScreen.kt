package com.wolfdeveloper.wolfdevlovers.application.feature.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.IconButton
import androidx.compose.material.Icon
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.wolfdeveloper.wolfdevlovers.application.feature.main.MainActivity
import com.wolfdeveloper.wolfdevlovers.application.feature.main.MainViewModel
import com.wolfdeveloper.wolfdevlovers.dsc.dimen.Size
import com.wolfdeveloper.wolfdevlovers.application.feature.detail.DetailViewModel.UiState
import com.wolfdeveloper.wolfdevlovers.commons.extensions.MAX_LINE_DEFAULT
import com.wolfdeveloper.wolfdevlovers.dsc.color.ColorPalette
import com.wolfdeveloper.wolfdevlovers.dsc.component.ImageLoader
import com.wolfdeveloper.wolfdevlovers.dsc.component.SpacerVertical
import com.wolfdeveloper.wolfdevlovers.dsc.dimen.Font
import com.wolfdeveloper.wolfdevlovers.dsc.dimen.Radius
import com.wolfdeveloper.wolfdevlovers.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.wolfdeveloper.wolfdevlovers.dsc.dimen.Weight

@Composable
fun DetailPageScreen(
    viewModel: DetailViewModel,
    flowViewModel: MainViewModel
) {
    val activity = LocalContext.current as MainActivity

    LaunchedEffect(viewModel) {
        viewModel.getDetails(
            activity = activity,
            id = flowViewModel.idLover
        )
    }

    Screen(
        uiState = viewModel.uiState,
        onClickMusic = {
            viewModel.onClickLink(activity = activity, url = it)
        },
        onClickClose = viewModel::onClickBack
    )

    EventConsumer(
        activity = activity,
        viewModel = viewModel,
        flowviewModel = flowViewModel
    )

}

@Composable
private fun Screen(
    uiState: UiState,
    onClickMusic: (url: String) -> Unit,
    onClickClose: () -> Unit
) = MaterialTheme {
    if (uiState.item.collectAsState().value.imageBackground.isNotEmpty()) {
        Image(
            painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(uiState.item.collectAsState().value.imageBackground)
                    .build()
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent,
    ) {
        val screenState by uiState.screenState.collectAsState()
        when (screenState) {
            is DetailViewModel.ScreenState.ScreenLoading -> ScreenProgress()
            is DetailViewModel.ScreenState.ScreenContent -> ScreenContent(
                uiState = uiState,
                onClickMusic = onClickMusic,
                onClickClose = onClickClose
            )
            is DetailViewModel.ScreenState.ScreenError -> {}
        }
    }
}

@Composable
fun ScreenContent(
    uiState: UiState,
    onClickMusic: (url: String) -> Unit,
    onClickClose: () -> Unit
) = Box(
    modifier = Modifier.fillMaxSize()
) {
    if (uiState.item.collectAsState().value.cardsVO.isNotEmpty()) {
        Content(uiState = uiState)
    }
    Header(
        uiState = uiState,
        onClickMusic = onClickMusic,
        onClickClose = onClickClose
    )
}

@Composable
fun Header(
    uiState: UiState,
    onClickMusic: (url: String) -> Unit,
    onClickClose: () -> Unit,
) = Column(
    modifier = Modifier
        .padding(horizontal = Size.Size16).fillMaxWidth(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    SpacerVertical(dp = WindowInsets.systemBars.asPaddingValues().calculateTopPadding())
    SpacerVertical(dp = Size.Size8)
    Row {
        IconButton(
            onClick = onClickClose
        ) {
            Icon(
                modifier = Modifier
                    .size(size = Size.Size32),
                imageVector = Icons.Rounded.ArrowBack,
                contentDescription = stringResource(id = R.string.accessibily_details_back)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(Weight.Weight_1)
        )
        if (uiState.item.value.cardsVO.firstOrNull()?.link.orEmpty().isNotEmpty()) {
            IconButton(
                modifier = Modifier
                    .padding(start = Size.Size4),
                onClick = {
                    onClickMusic.invoke(
                        uiState.item.value.cardsVO.firstOrNull()?.link.orEmpty()
                    )
                }
            ) {
                Icon(
                    modifier = Modifier.size(Size.Size24),
                    painter = painterResource(id = R.drawable.ic_link),
                    contentDescription = stringResource(id = R.string.accessibily_details_play),
                    tint = Color.Unspecified
                )
            }
        }
    }
}

@Composable
fun Content(
    uiState: UiState
) = Column(
    modifier = Modifier.verticalScroll(rememberScrollState())
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(Radius.Radius8),
        backgroundColor = ColorPalette.White
    ) {
        Column {
            ImageLoader(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = Size.Size450),
                image = uiState.item.collectAsState().value.cardsVO.firstOrNull()?.image.orEmpty(),
                contentDescription = uiState.item.collectAsState().value.cardsVO.firstOrNull()?.name.orEmpty()
            )
            SpacerVertical()
            Text(
                modifier = Modifier.padding(
                    horizontal = Size.Size8
                ),
                text = uiState.item.collectAsState().value.cardsVO.firstOrNull()?.name.orEmpty(),
                fontWeight = FontWeight.SemiBold,
                fontSize = Font.Font20,
                fontFamily = FontFamily.SansSerif,
                overflow = TextOverflow.Ellipsis,
                maxLines = MAX_LINE_DEFAULT
            )
            SpacerVertical(dp = Size.Size16)
            Text(
                modifier = Modifier.padding(
                    horizontal = Size.Size8
                ),
                text = uiState.item.collectAsState().value.cardsVO.firstOrNull()?.description.orEmpty(),
                fontWeight = FontWeight.Normal,
                fontSize = Font.Font20,
                fontFamily = FontFamily.SansSerif
            )
            SpacerVertical(dp = Size.Size16)
        }
    }
}

@Composable
private fun ScreenProgress() = Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(Size.SizeProgress),
            color = ColorPalette.Black
        )
    }
}


@Composable
private fun EventConsumer(
    activity: MainActivity,
    viewModel: DetailViewModel,
    flowviewModel: MainViewModel
) {
    LaunchedEffect(key1 = viewModel) {
        viewModel.eventsFlow.collect { event ->
            when (event) {
                DetailViewModel.ScreenEvent.GoBack -> activity.onBackPressed()
                is DetailViewModel.ScreenEvent.NavigateTo -> flowviewModel.navigate(event.navigation)
            }
        }
    }
}
