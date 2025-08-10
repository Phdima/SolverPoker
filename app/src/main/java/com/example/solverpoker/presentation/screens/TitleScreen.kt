package com.example.solverpoker.presentation.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.solverpoker.presentation.components.AppHintDialog
import com.example.solverpoker.presentation.viewmodel.HintViewModel
import kotlinx.coroutines.delay


@Composable
fun TitleScreen(
    navController: NavHostController,
    hintViewModel: HintViewModel = hiltViewModel(),
) {
    var isDialogVisible by remember { mutableStateOf(false) }
    val showAppHint by hintViewModel.showAppHint.collectAsStateWithLifecycle()

    val alpha by animateFloatAsState(
        targetValue = if (showAppHint) 1f else 0f,
        animationSpec = tween(durationMillis = 750)
    )

    LaunchedEffect(showAppHint) {
        if (showAppHint) {
            isDialogVisible = true
        } else {

            delay(750)
            isDialogVisible = false
        }
    }

    Box()
    {
        Column(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    navController.navigate("PreflopChart")
                },
                colors = ButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .width(150.dp)
            ) {
                Text("Preflop Chart")
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    navController.navigate("TrainerScreen")
                },
                modifier = Modifier
                    .width(150.dp),
                colors = ButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary
                ),

                ) {
                Text("Poker Trainer")
            }

        }
        if (isDialogVisible) {
            AppHintDialog(
                onDismiss = hintViewModel::markAppHintShown,
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer(alpha = alpha)
            )
        }
    }
}