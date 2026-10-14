package com.acme.transport.customs.hold.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredHoldEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredHoldEstimate("code-499", "name-70")
        val view = toTieredHoldEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredHoldEstimateViews(mutableListOf<TieredHoldEstimate>(item, item)).size, "views")
    }
}
