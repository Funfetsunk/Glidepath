package com.glidepath.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Corner radii used across Glidepath (§7): cards 16, buttons 18, widget 24, tiles 12. */
object GlideShapes {
    val card = RoundedCornerShape(16.dp)
    val button = RoundedCornerShape(18.dp)
    val tile = RoundedCornerShape(12.dp)
    val chip = RoundedCornerShape(10.dp)
    val widget = RoundedCornerShape(24.dp)
}
