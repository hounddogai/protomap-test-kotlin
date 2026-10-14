package com.acme.transport.emissions.evcharge.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryEvChargeThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryEvChargeThreshold("code-472", "name-292")
        val view = toPrimaryEvChargeThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryEvChargeThresholdViews(mutableListOf<PrimaryEvChargeThreshold>(item, item)).size, "views")
    }
}
