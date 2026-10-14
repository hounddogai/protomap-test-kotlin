package com.acme.transport.customs.hold.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class HoldVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHoldVariance("code-596", "name-371")
        val view = toHoldVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHoldVarianceViews(mutableListOf<HoldVariance>(item, item)).size, "views")
    }
}
