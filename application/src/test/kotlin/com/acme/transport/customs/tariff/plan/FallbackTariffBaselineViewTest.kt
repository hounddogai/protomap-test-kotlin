package com.acme.transport.customs.tariff.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackTariffBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackTariffBaseline("code-485", "name-619")
        val view = toFallbackTariffBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackTariffBaselineViews(mutableListOf<FallbackTariffBaseline>(item, item)).size, "views")
    }
}
