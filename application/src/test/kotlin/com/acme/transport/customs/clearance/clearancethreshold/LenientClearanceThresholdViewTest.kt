package com.acme.transport.customs.clearance.clearancethreshold

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientClearanceThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientClearanceThreshold("code-523", "label-785")
        val view = toLenientClearanceThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientClearanceThresholdViews(mutableListOf<LenientClearanceThreshold>(item, item)).size, "views")
    }
}
