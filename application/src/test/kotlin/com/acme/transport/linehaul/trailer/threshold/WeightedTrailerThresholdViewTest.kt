package com.acme.transport.linehaul.trailer.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedTrailerThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedTrailerThreshold("code-434", "name-758")
        val view = toWeightedTrailerThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedTrailerThresholdViews(mutableListOf<WeightedTrailerThreshold>(item, item)).size, "views")
    }
}
