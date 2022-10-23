package com.teste.poc.application.feature.dashboard

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import coil.compose.rememberImagePainter
import com.teste.poc.R
import com.teste.poc.application.feature.main.MainActivity
import com.teste.poc.application.feature.main.MainViewModel
import com.teste.poc.application.feature.dashboard.DashboardViewModel.ScreenEvent
import com.teste.poc.application.feature.dashboard.DashboardViewModel.UiState
import com.teste.poc.commons.extensions.MAX_LINE_DEFAULT
import com.teste.poc.commons.extensions.isNull
import com.teste.poc.dsc.color.ColorPalette
import com.teste.poc.dsc.component.SpacerHorizontal
import com.teste.poc.dsc.component.SpacerVertical
import com.teste.poc.dsc.component.rippleClickable
import com.teste.poc.dsc.dimen.Font
import com.teste.poc.dsc.dimen.Size

@Composable
fun DashboardPageScreen(
    viewModel: DashboardViewModel,
    flowViewModel: MainViewModel
) {
    val activity = LocalContext.current as MainActivity
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(viewModel) {
        viewModel.setup()
    }

    Screen(
        uiState = viewModel.uiState,
        onClickCode = {
            clipboardManager.setText(AnnotatedString(viewModel.uiState.code.value))
            Toast.makeText(
                activity,
                R.string.dash_success_code_copy,
                Toast.LENGTH_LONG
            )
        },
        onClick = {
            viewModel.insert(activity = activity)
        },
        onClickAccess = viewModel::onClickAccess,
        onClickInsert = viewModel::onClickInsert
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
    onClick: () -> Unit,
    onClickCode: () -> Unit,
    onClickAccess: () -> Unit,
    onClickInsert: () -> Unit,
) = MaterialTheme {
    Box(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent,
        ) {
            val screenState by uiState.screenState.collectAsState()
            when (screenState) {
                is DashboardViewModel.ScreenState.ScreenDashBoard -> ScreenDashBoard(
                    uiState = uiState,
                    onClickAccess = onClickAccess,
                    onClickInsert = onClickInsert,
                )
                is DashboardViewModel.ScreenState.ScreenLoading -> ScreenProgress()
                is DashboardViewModel.ScreenState.ScreenContent -> ScreenContent(
                    uiState = uiState,
                    onClick = onClick
                )
                is DashboardViewModel.ScreenState.ScreenSuccess -> ScreenSuccess(
                    uiState = uiState,
                    onClickAccess = onClickAccess,
                    onClickCode = onClickCode
                )
                is DashboardViewModel.ScreenState.ScreenError -> {

                }
            }
        }
    }
}

@Composable
private fun ScreenDashBoard(
    uiState: UiState,
    onClickAccess: () -> Unit,
    onClickInsert: () -> Unit,
) = Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Size.Size32),
            value = uiState.code.value,
            onValueChange = {
                if (it.length <= MAX_CARACHTER) {
                    uiState.code.value = it
                }
            },
            label = { Text(stringResource(id = R.string.dash_code)) },
            maxLines = MAX_LINE_DEFAULT,
            textStyle = TextStyle(
                color = Color.Black,
                fontWeight = FontWeight.Normal
            ),
        )
        SpacerVertical(dp = Size.Size32)
        Button(
            onClick = onClickAccess,
            contentPadding = PaddingValues(
                start = Size.Size32,
                end = Size.Size32,
                top = Size.Size16,
                bottom = Size.Size16
            )
        ) {
            Icon(
                Icons.Filled.Code,
                contentDescription = stringResource(id = R.string.dash_button_access),
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(id = R.string.dash_button_access))
        }
        SpacerVertical(dp = Size.Size32)
        Button(
            onClick = onClickInsert,
            contentPadding = PaddingValues(
                start = Size.Size32,
                end = Size.Size32,
                top = Size.Size16,
                bottom = Size.Size16
            )
        ) {
            Icon(
                Icons.Filled.Create,
                contentDescription = stringResource(id = R.string.dash_button_create),
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(id = R.string.dash_button_create))
        }
    }
}

@Composable
fun ScreenContent(
    uiState: UiState,
    onClick: () -> Unit
) = Column(
    modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
) {
    SpacerVertical(dp = Size.Size32)
    Header()
    SpacerVertical(dp = Size.Size16)
    formData(uiState)
    SpacerVertical(dp = Size.Size16)
    TitleLover()
    SpacerVertical(dp = Size.Size16)
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState())
    ) {
        uiState.lovers.forEachIndexed { index, it ->
            formDataLovers(lover = it, index + ONE)
        }
    }
    SpacerVertical(dp = Size.Size16)
    Button(onClick)
    SpacerVertical(dp = Size.Size32)
}

@Composable
fun Header() = Column(
    modifier = Modifier
        .padding(
            top = Size.Size4,
            bottom = Size.Size4,
            start = Size.Size16,
            end = Size.Size16,
        )
        .fillMaxWidth(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    Text(
        text = stringResource(id = R.string.app_name),
        fontSize = Font.Font25,
        color = ColorPalette.Black,
        textAlign = TextAlign.Center,
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun formData(uiState: UiState) = Column(
    modifier = Modifier
        .padding(
            top = Size.Size4,
            bottom = Size.Size4,
            start = Size.Size16,
            end = Size.Size16,
        )
        .fillMaxWidth()
) {
    //Textfield Name
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = uiState.name.value,
        onValueChange = {
            if (it.length <= MAX_CARACHTER) {
                uiState.name.value = it
            }
        },
        label = { Text(stringResource(id = R.string.dash_name)) },
        maxLines = MAX_LINE_DEFAULT,
        textStyle = TextStyle(
            color = Color.Black,
            fontWeight = FontWeight.Normal
        ),
    )
    SpacerVertical()
    //Textfield Name Lover
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = uiState.nameLover.value,
        onValueChange = {
            if (it.length <= MAX_CARACHTER) {
                uiState.nameLover.value = it
            }
        },
        label = { Text(stringResource(id = R.string.dash_his_name)) },
        maxLines = MAX_LINE_DEFAULT,
        textStyle = TextStyle(
            color = Color.Black,
            fontWeight = FontWeight.Normal
        ),
    )
    SpacerVertical()
    //Textfield Plus
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = uiState.plus.value,
        onValueChange = {
            if (it.length <= MAX_CARACHTER_PLUS) {
                uiState.plus.value = it
            }
        },
        label = { Text(stringResource(id = R.string.dash_plus)) },
        maxLines = MAX_LINE_PLUS,
        textStyle = TextStyle(
            color = Color.Black,
            fontWeight = FontWeight.Normal
        ),
    )
    SpacerVertical()
    //Textfield Spotify
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = uiState.spotify.value,
        onValueChange = {
            if (it.length <= MAX_CARACHTER_PLUS) {
                uiState.spotify.value = it
            }
        },
        label = { Text(stringResource(id = R.string.dash_spotify)) },
        maxLines = MAX_LINE_DEFAULT,
        textStyle = TextStyle(
            color = Color.Black,
            fontWeight = FontWeight.Normal
        ),
    )
    SpacerVertical()
    //Textfield Instagram
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = uiState.instagram.value,
        onValueChange = {
            if (it.length <= MAX_CARACHTER) {
                uiState.instagram.value = it
            }
        },
        label = { Text(stringResource(id = R.string.dash_instagram)) },
        maxLines = MAX_LINE_PLUS,
        textStyle = TextStyle(
            color = Color.Black,
            fontWeight = FontWeight.Normal
        ),
    )
    SpacerVertical()
    //Textfield Whatssap
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = uiState.whatssap.value,
        onValueChange = {
            if (it.length <= MAX_CARACHTER) {
                uiState.whatssap.value = it
            }
        },
        label = { Text(stringResource(id = R.string.dash_whatssap)) },
        maxLines = MAX_LINE_PLUS,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        textStyle = TextStyle(
            color = Color.Black,
            fontWeight = FontWeight.Normal
        )
    )
    SpacerVertical()
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        ImageProfile(
            uiState
        )
        SpacerHorizontal(Size.Size32)
        ImageBackground(
            uiState
        )
    }

    SpacerVertical()
}

@Composable
fun ImageProfile(uiState: UiState) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uiState.profileImage.value = uri
    }
    return Column(
        modifier = Modifier.rippleClickable {
            launcher.launch(LAUNCH_IMAGE)
        },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.dash_image_profile),
            fontSize = Font.Font16,
            color = ColorPalette.Black,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal
        )
        SpacerVertical(Size.Size8)
        if (uiState.profileImage.collectAsState().value.isNull()) {
            Icon(
                Icons.Filled.Add, contentDescription = null,
                modifier = Modifier
                    .size(Size.Size100)
            )
        } else {
            Image(
                painter = rememberImagePainter(
                    uiState.profileImage.value
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(Size.Size100)
            )
        }
    }
}

@Composable
fun ImageBackground(uiState: UiState) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uiState.backgroundImage.value = uri
    }
    return Column(
        modifier = Modifier.rippleClickable {
            launcher.launch(LAUNCH_IMAGE)
        },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.dash_image_background),
            fontSize = Font.Font16,
            color = ColorPalette.Black,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal
        )
        SpacerVertical(Size.Size8)
        if (uiState.backgroundImage.collectAsState().value.isNull()) {
            Icon(
                Icons.Filled.Add, contentDescription = null,
                modifier = Modifier.size(Size.Size100)
            )
        } else {
            Image(
                painter = rememberImagePainter(
                    uiState.backgroundImage.value
                ),
                contentDescription = null,
                modifier = Modifier.size(Size.Size100)
            )
        }
    }
}

@Composable
fun TitleLover() = Column(
    modifier = Modifier
        .padding(
            top = Size.Size4,
            bottom = Size.Size4,
            start = Size.Size16,
            end = Size.Size16,
        )
        .fillMaxWidth(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    Text(
        text = stringResource(id = R.string.menu_toast),
        fontSize = Font.Font16,
        color = ColorPalette.Black,
        textAlign = TextAlign.Center,
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal
    )
}

@Composable
private fun formDataLovers(lover: UiState.LoverState, index: Int) = Column(
    modifier = Modifier
        .padding(
            top = Size.Size4,
            bottom = Size.Size4,
            start = Size.Size16,
            end = Size.Size16,
        )
        .fillMaxWidth(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text(
        text = index.toString(),
        fontSize = Font.Font16,
        color = ColorPalette.Black,
        textAlign = TextAlign.Center,
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal
    )
    SpacerVertical(Size.Size8)
    //Textfield Text
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .width(
                Size.Size370
            ),
        value = lover.text.value,
        onValueChange = {
            if (it.length <= MAX_CARACHTER_TEXT) {
                lover.text.value = it
            }
        },
        label = { Text(stringResource(id = R.string.dash_his_text)) },
        maxLines = MAX_LINE_TEXT,
        textStyle = TextStyle(
            color = Color.Black,
            fontWeight = FontWeight.Normal
        ),
    )
    SpacerVertical()
    //Textfield Music
    TextField(
        modifier = Modifier
            .fillMaxWidth()
            .width(
                Size.Size370
            ),
        value = lover.music.value,
        onValueChange = {
            if (it.length <= MAX_CARACHTER) {
                lover.music.value = it
            }
        },
        label = { Text(stringResource(id = R.string.dash_his_music)) },
        maxLines = MAX_LINE_DEFAULT,
        textStyle = TextStyle(
            color = Color.Black,
            fontWeight = FontWeight.Normal
        ),
    )
    SpacerVertical()
    ImageLover(lover)
}

@Composable
fun ImageLover(lover: UiState.LoverState) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        lover.Image.value = uri
    }
    return Column(
        modifier = Modifier
            .fillMaxWidth()
            .rippleClickable {
                launcher.launch(LAUNCH_IMAGE)
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = R.string.dash_image_lover),
            fontSize = Font.Font16,
            color = ColorPalette.Black,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal
        )
        SpacerVertical(Size.Size8)
        if (lover.Image.collectAsState().value.isNull()) {
            Icon(
                Icons.Filled.Add, contentDescription = null,
                modifier = Modifier
                    .size(Size.Size350)
            )
        } else {
            Image(
                painter = rememberImagePainter(
                    lover.Image.value
                ),
                contentDescription = null,
                modifier = Modifier
                    .size(Size.Size350)
            )
        }
    }
}

@Composable
private fun Button(onClick: () -> Unit) = Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Button(
        onClick = onClick,
        contentPadding = PaddingValues(
            start = Size.Size32,
            end = Size.Size32,
            top = Size.Size16,
            bottom = Size.Size16
        )
    ) {
        Icon(
            Icons.Filled.Favorite,
            contentDescription = stringResource(id = R.string.dash_button),
            modifier = Modifier.size(ButtonDefaults.IconSize)
        )
        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
        Text(stringResource(id = R.string.dash_button))
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
        Text(stringResource(id = R.string.dash_loading))
        SpacerVertical(dp = Size.Size32)
        CircularProgressIndicator(
            modifier = Modifier.size(Size.SizeProgress),
            color = ColorPalette.Black
        )
    }
}

@Composable
private fun ScreenSuccess(
    uiState: UiState,
    onClickAccess: () -> Unit,
    onClickCode: () -> Unit
) = Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier.rippleClickable {
                onClickCode.invoke()
            },
            text = stringResource(id = R.string.dash_success_code, uiState.code.value),
            fontSize = Font.Font25,
            color = ColorPalette.Black,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Normal
        )
        SpacerVertical(dp = Size.Size32)
        Button(
            onClick = onClickCode,
            contentPadding = PaddingValues(
                start = Size.Size32,
                end = Size.Size32,
                top = Size.Size16,
                bottom = Size.Size16
            )
        ) {
            Icon(
                Icons.Filled.CopyAll,
                contentDescription = stringResource(id = R.string.dash_button_copy),
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(id = R.string.dash_button_copy))
        }
        SpacerVertical(dp = Size.Size32)
        Button(
            onClick = onClickAccess,
            contentPadding = PaddingValues(
                start = Size.Size32,
                end = Size.Size32,
                top = Size.Size16,
                bottom = Size.Size16
            )
        ) {
            Icon(
                Icons.Filled.SkipNext,
                contentDescription = stringResource(id = R.string.dash_button_next),
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text(stringResource(id = R.string.dash_button_next))
        }
    }
}

@Composable
private fun EventConsumer(
    activity: MainActivity,
    viewModel: DashboardViewModel,
    flowviewModel: MainViewModel
) {
    LaunchedEffect(key1 = viewModel) {
        viewModel.eventsFlow.collect { event ->
            when (event) {
                ScreenEvent.GoBack -> activity.onBackPressed()
                is ScreenEvent.NavigateTo -> flowviewModel.navigate(event.navigation)
            }
        }
    }
}

private const val ONE = 1
private const val MAX_LINE_PLUS = 5
private const val MAX_CARACHTER = 100
private const val MAX_CARACHTER_PLUS = 250
private const val MAX_LINE_TEXT = 50
private const val MAX_CARACHTER_TEXT = 700
private const val LAUNCH_IMAGE = "image/*"
