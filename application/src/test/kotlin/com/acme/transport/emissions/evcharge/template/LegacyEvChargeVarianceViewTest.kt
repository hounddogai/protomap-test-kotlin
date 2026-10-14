package com.acme.transport.emissions.evcharge.template

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyEvChargeVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyEvChargeVariance("code-4", "title-661")
        val view = toLegacyEvChargeVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyEvChargeVarianceViews(mutableListOf<LegacyEvChargeVariance>(item, item)).size, "views")
    }
}
