@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.example.compose.snippets.animations.sharedelement

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.compose.snippets.R

private val listSnacks = listOf(
    Snack("Cupcake", "", R.drawable.cupcake),
    Snack("Donut", "", R.drawable.donut),
    Snack("Eclair", "", R.drawable.eclair),
    Snack("Froyo", "", R.drawable.froyo),
    Snack("Gingerbread", "", R.drawable.gingerbread),
    Snack("Honeycomb", "", R.drawable.honeycomb),
)

fun Modifier.blurLayer(layer: GraphicsLayer, radius: Float): Modifier {
    return if (radius == 0f) this else this.drawWithContent {
        layer.apply {
            record {
                this@drawWithContent.drawContent()
            }
            // will apply a blur on API 31+
            this.renderEffect = BlurEffect(radius, radius, TileMode.Decal)
        }
        drawLayer(layer)
    }
}

private fun <T> animationSpec() = tween<T>(durationMillis = 500)
private val boundsTransition = BoundsTransform { _, _ -> animationSpec() }
private val shapeForSharedElement = RoundedCornerShape(16.dp)

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview
@Composable
private fun AnimatedVisibilitySharedElementFullExample() {
    var selectedSnack by remember { mutableStateOf<Snack?>(null) }
    val graphicsLayer = rememberGraphicsLayer()
    val animateBlurRadius = animateFloatAsState(
        targetValue = if (selectedSnack != null) 20f else 0f,
        label = "blur radius",
        animationSpec = animationSpec()
    )

    SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray.copy(alpha = 0.5f))
                .blurLayer(graphicsLayer, animateBlurRadius.value)
                .padding(16.dp),
            columns = GridCells.Adaptive(150.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(listSnacks) { index, snack ->
                SnackItem(
                    snack = snack,
                    onClick = {
                        selectedSnack = snack
                    },
                    visible = selectedSnack != snack
                )
            }
        }

        SnackEditDetails(
            snack = selectedSnack,
            onConfirmClick = {
                selectedSnack = null
            }
        )
    }
}

@Composable
fun SharedTransitionScope.SnackItem(
    snack: Snack,
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn(animationSpec = animationSpec()) + scaleIn(
            animationSpec()
        ),
        exit = fadeOut(animationSpec = animationSpec()) + scaleOut(
            animationSpec()
        )
    ) {
        Box(
            modifier = Modifier
                .sharedBounds(
                    sharedContentState = rememberSharedContentState(key = "${snack.name}-bounds"),
                    animatedVisibilityScope = this,
                    boundsTransform = boundsTransition,
                    clipInOverlayDuringTransition = OverlayClip(shapeForSharedElement)
                )
                .background(Color.White, shapeForSharedElement)
                .clip(shapeForSharedElement)
        ) {
            SnackContents(
                snack = snack,
                modifier = Modifier.sharedElement(
                    state = rememberSharedContentState(key = snack.name),
                    animatedVisibilityScope = this@AnimatedVisibility,
                    boundsTransform = boundsTransition,
                ),
                onClick = onClick
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalLayoutApi::class)
@Preview
@Composable
private fun AnimatedVisibilitySharedElementShortenedExample() {
    // [START android_compose_shared_elements_animated_visibility]
    var selectedSnack by remember { mutableStateOf<Snack?>(null) }

    SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            // [START_EXCLUDE]
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray.copy(alpha = 0.5f))
                .padding(16.dp),
            columns = GridCells.Adaptive(150.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
            // [END_EXCLUDE]
        ) {
            itemsIndexed(listSnacks) { index, snack ->
                AnimatedVisibility(
                    visible = snack != selectedSnack,
                    // [START_EXCLUDE]
                    enter = fadeIn(animationSpec = animationSpec()) + scaleIn(
                        animationSpec()
                    ),
                    exit = fadeOut(animationSpec = animationSpec()) + scaleOut(
                        animationSpec()
                    )
                    // [END_EXCLUDE]
                ) {
                    Box(
                        modifier = Modifier
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState(key = "${snack.name}-bounds"),
                                // Using the scope provided by AnimatedVisibility
                                animatedVisibilityScope = this,
                                boundsTransform = boundsTransition,
                                clipInOverlayDuringTransition = OverlayClip(shapeForSharedElement)
                            )
                            .background(Color.White, shapeForSharedElement)
                            .clip(shapeForSharedElement)
                    ) {
                        SnackContents(
                            snack = snack,
                            modifier = Modifier.sharedElement(
                                state = rememberSharedContentState(key = snack.name),
                                animatedVisibilityScope = this@AnimatedVisibility,
                                boundsTransform = boundsTransition,
                            ),
                            onClick = {
                                selectedSnack = snack
                            }
                        )
                    }
                }
            }
        }

        SnackEditDetails(
            snack = selectedSnack,
            onConfirmClick = {
                selectedSnack = null
            }
        )
    }
    // [END android_compose_shared_elements_animated_visibility]
}

@Composable
fun SharedTransitionScope.SnackEditDetails(
    snack: Snack?,
    modifier: Modifier = Modifier,
    onConfirmClick: () -> Unit
) {
    AnimatedContent(
        modifier = modifier,
        targetState = snack,
        transitionSpec = {
            fadeIn(animationSpec()) togetherWith fadeOut(animationSpec())
        },
        label = "SnackEditDetails"
    ) { targetSnack ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (targetSnack != null) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        onConfirmClick()
                    }
                    .background(Color.Black.copy(alpha = 0.5f)))
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "${targetSnack.name}-bounds"),
                            animatedVisibilityScope = this@AnimatedContent,
                            boundsTransform = boundsTransition,
                            clipInOverlayDuringTransition = OverlayClip(shapeForSharedElement)
                        )
                        .background(Color.White, shapeForSharedElement)
                        .clip(shapeForSharedElement)
                ) {

                    SnackContents(
                        snack = targetSnack,
                        modifier = Modifier.sharedElement(
                            state = rememberSharedContentState(key = targetSnack.name),
                            animatedVisibilityScope = this@AnimatedContent,
                            boundsTransform = boundsTransition,
                        ),
                        onClick = {
                            onConfirmClick()
                        }
                    )
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { onConfirmClick() }) {
                            Text(text = "Save changes")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SnackContents(
    snack: Snack,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onClick()
            }
    ) {
        Image(
            painter = painterResource(id = snack.image),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentScale = ContentScale.Crop,
            contentDescription = null
        )
        Text(
            text = snack.name,
            modifier = Modifier
                .wrapContentWidth()
                .padding(8.dp),
            style = MaterialTheme.typography.titleSmall
        )
    }
}