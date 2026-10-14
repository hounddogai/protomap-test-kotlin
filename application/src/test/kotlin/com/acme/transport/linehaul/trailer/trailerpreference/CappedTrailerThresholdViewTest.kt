package com.acme.transport.linehaul.trailer.trailerpreference

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedTrailerThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedTrailerThreshold("code-715", "label-475")
        val view = toCappedTrailerThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedTrailerThresholdViews(mutableListOf<CappedTrailerThreshold>(item, item)).size, "views")
    }
}
