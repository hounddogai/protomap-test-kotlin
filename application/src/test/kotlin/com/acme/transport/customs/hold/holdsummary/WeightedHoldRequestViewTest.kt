package com.acme.transport.customs.hold.holdsummary

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedHoldRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedHoldRequest("code-753", "name-990")
        val view = toWeightedHoldRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedHoldRequestViews(mutableListOf<WeightedHoldRequest>(item, item)).size, "views")
    }
}
