package com.acme.transport.safety.cameraevent.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedCameraEventTierViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedCameraEventTier("code-935", "name-471")
        val view = toWeightedCameraEventTierView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedCameraEventTierViews(mutableListOf<WeightedCameraEventTier>(item, item)).size, "views")
    }
}
