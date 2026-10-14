package com.acme.transport.emissions.evcharge.regionalchargeslot

import kotlin.test.Test
import kotlin.test.assertEquals

class EvChargeVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEvChargeVariance("code-639", "name-984")
        val view = toEvChargeVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEvChargeVarianceViews(mutableListOf<EvChargeVariance>(item, item)).size, "views")
    }
}
