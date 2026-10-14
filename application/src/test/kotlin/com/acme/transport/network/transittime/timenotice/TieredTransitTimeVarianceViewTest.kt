package com.acme.transport.network.transittime.timenotice

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredTransitTimeVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredTransitTimeVariance("code-85", "name-576")
        val view = toTieredTransitTimeVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredTransitTimeVarianceViews(mutableListOf<TieredTransitTimeVariance>(item, item)).size, "views")
    }
}
