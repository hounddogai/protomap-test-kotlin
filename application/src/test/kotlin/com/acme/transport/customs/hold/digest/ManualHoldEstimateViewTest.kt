package com.acme.transport.customs.hold.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualHoldEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualHoldEstimate("code-96", "title-495")
        val view = toManualHoldEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualHoldEstimateViews(mutableListOf<ManualHoldEstimate>(item, item)).size, "views")
    }
}
