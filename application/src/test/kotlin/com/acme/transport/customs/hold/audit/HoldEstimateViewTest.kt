package com.acme.transport.customs.hold.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class HoldEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHoldEstimate("code-547", "name-507")
        val view = toHoldEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHoldEstimateViews(mutableListOf<HoldEstimate>(item, item)).size, "views")
    }
}
