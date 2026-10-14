package com.acme.transport.emissions.evcharge.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientEvChargeThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientEvChargeThreshold("code-333", "title-475")
        val view = toLenientEvChargeThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientEvChargeThresholdViews(mutableListOf<LenientEvChargeThreshold>(item, item)).size, "views")
    }
}
