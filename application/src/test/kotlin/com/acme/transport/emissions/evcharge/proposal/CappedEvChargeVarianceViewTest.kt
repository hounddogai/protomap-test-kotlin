package com.acme.transport.emissions.evcharge.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedEvChargeVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedEvChargeVariance("code-495", "label-691")
        val view = toCappedEvChargeVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedEvChargeVarianceViews(mutableListOf<CappedEvChargeVariance>(item, item)).size, "views")
    }
}
