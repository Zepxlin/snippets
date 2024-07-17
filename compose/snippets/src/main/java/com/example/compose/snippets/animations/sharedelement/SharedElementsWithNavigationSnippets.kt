/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.example.compose.snippets.animations.sharedelement

import androidx.activity.BackEventCompat
import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.currentState
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.compose.snippets.R
import com.example.compose.snippets.lists.Book
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

private val listSnacks = listOf(
    Snack("Cupcake", "", R.drawable.cupcake),
    Snack("Donut", "", R.drawable.donut),
    Snack("Eclair", "", R.drawable.eclair),
    Snack("Froyo", "", R.drawable.froyo),
    Snack("Gingerbread", "", R.drawable.gingerbread),
    Snack("Honeycomb", "", R.drawable.honeycomb),
)

// [START android_compose_shared_element_predictive_back]
@Preview
@Composable
fun SharedElement_PredictiveBack() {
    SharedTransitionLayout {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = "home"
        ) {
            composable("home") {
                HomeScreen(
                    this@SharedTransitionLayout,
                    this@composable,
                    onItemClick = { index ->
                        navController.navigate("details/$index")
                    }
                )
            }
            composable(
                "details/{item}",
                arguments = listOf(navArgument("item") { type = NavType.IntType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("item")
                val snack = listSnacks[id!!]
                DetailsScreen(
                    id,
                    snack,
                    this@SharedTransitionLayout,
                    this@composable,
                    touchYProvider = {
                        0f
                    },
                    onBackPressed = {
                        navController.navigate("home")
                    }
                )
            }
        }
    }
}

@Composable
private fun DetailsScreen(
    id: Int,
    snack: Snack,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    touchYProvider: () -> Float,
    onBackPressed: () -> Unit
) {
    with(sharedTransitionScope) {
        Column(
            Modifier
                .fillMaxSize()
                .clickable {
                    onBackPressed()
                }
        ) {
            Image(
                painterResource(id = snack.image),
                contentDescription = snack.description,
                contentScale = ContentScale.Crop,
                modifier = Modifier

                    .sharedElement(
                        sharedTransitionScope.rememberSharedContentState(key = "image-$id"),
                        animatedVisibilityScope = animatedContentScope
                    )
                    .aspectRatio(1f)
                    .fillMaxWidth()
                    .graphicsLayer {
                        println("graphics layer ${touchYProvider.invoke()}")
                        this.translationY = touchYProvider()
                    }
            )
            Text(
                snack.name, fontSize = 18.sp,
                modifier = Modifier

                    .sharedElement(
                        sharedTransitionScope.rememberSharedContentState(key = "text-$id"),
                        animatedVisibilityScope = animatedContentScope
                    )
                    .fillMaxWidth()
                    .graphicsLayer {
                        this.translationY = touchYProvider()
                    }
            )
        }
    }
}

@Composable
private fun HomeScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope,
    onItemClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(listSnacks) { index, item ->
            Row(
                Modifier.clickable {
                    onItemClick(index)
                }
            ) {
                Spacer(modifier = Modifier.width(8.dp))
                with(sharedTransitionScope) {
                    Image(
                        painterResource(id = item.image),
                        contentDescription = item.description,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .sharedElement(
                                sharedTransitionScope.rememberSharedContentState(key = "image-$index"),
                                animatedVisibilityScope = animatedContentScope
                            )
                            .size(100.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        item.name, fontSize = 18.sp,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .sharedElement(
                                sharedTransitionScope.rememberSharedContentState(key = "text-$index"),
                                animatedVisibilityScope = animatedContentScope,
                            )
                    )
                }
            }
        }
    }
}

data class Snack(
    val name: String,
    val description: String,
    @DrawableRes val image: Int
)
// [END android_compose_shared_element_predictive_back]


private sealed class Screen {
    data object Home : Screen()
    data class Details(val id: Int) : Screen()
}

@Preview
@Composable
fun CustomPredictiveBackHandle() {
    // [START android_compose_shared_element_custom_seeking]
    val seekableTransitionState = remember {
        SeekableTransitionState<Screen>(Screen.Home)
    }
    val transition = rememberTransition(transitionState = seekableTransitionState)
    var latestBackEvent by remember {
        mutableStateOf<BackEventCompat?>(null)
    }
    var touchYDiff by remember {
        mutableStateOf<Float>(0f)
    }

    PredictiveBackHandler(seekableTransitionState.currentState is Screen.Details) { progress ->
        try {
            progress.collect { backEvent ->
                // code for progress
                try {
                    seekableTransitionState.seekTo(backEvent.progress, targetState = Screen.Home)
                    touchYDiff = backEvent.touchY - (latestBackEvent?.touchY ?: 0f)
                    latestBackEvent = backEvent
                } catch (e: CancellationException) {
                    // ignore
                    latestBackEvent = null
                }
            }
            // code for completion
            seekableTransitionState.animateTo(seekableTransitionState.targetState)
            latestBackEvent = null
        } catch (e: CancellationException) {
            // code for cancellation
            seekableTransitionState.animateTo(seekableTransitionState.currentState)
            latestBackEvent = null
        }
    }
    val coroutineScope = rememberCoroutineScope()
    var lastNavigatedIndex by remember {
        mutableIntStateOf(0)
    }
    Column {
        Slider(modifier = Modifier.height(48.dp),
            value = seekableTransitionState.fraction,
            onValueChange = {
                coroutineScope.launch {
                    if (seekableTransitionState.currentState is Screen.Details){
                        seekableTransitionState.seekTo(it, Screen.Home)
                    } else {
                        // seek to the previously navigated index
                        seekableTransitionState.seekTo(it, Screen.Details(lastNavigatedIndex))
                    }
                }})
        SharedTransitionLayout(modifier = Modifier.weight(1f)) {
            transition.AnimatedContent { targetState ->
                when (targetState) {
                    Screen.Home -> {
                        HomeScreen(
                            this@SharedTransitionLayout,
                            this@AnimatedContent,
                            onItemClick = {
                                coroutineScope.launch {
                                    lastNavigatedIndex = it
                                    seekableTransitionState.animateTo(Screen.Details(it))
                                }
                            }
                        )
                    }

                    is Screen.Details -> {
                        val snack = listSnacks[targetState.id]
                        DetailsScreen(
                            targetState.id,
                            snack,
                            this@SharedTransitionLayout,
                            this@AnimatedContent,
                            touchYProvider = {
                                touchYDiff
                            },
                            onBackPressed = {
                                coroutineScope.launch {
                                    seekableTransitionState.animateTo(Screen.Home)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // [END android_compose_shared_element_custom_seeking]
}