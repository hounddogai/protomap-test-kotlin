package com.acme.transport.emissions.evcharge.chargepreference

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackEvChargeThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackEvChargeThreshold("code-574", "title-52")
        val view = toFallbackEvChargeThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackEvChargeThresholdViews(mutableListOf<FallbackEvChargeThreshold>(item, item)).size, "views")
    }
}
