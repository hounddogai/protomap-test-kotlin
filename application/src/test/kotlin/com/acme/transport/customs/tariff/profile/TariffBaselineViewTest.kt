package com.acme.transport.customs.tariff.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class TariffBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTariffBaseline("code-800", "title-585")
        val view = toTariffBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTariffBaselineViews(mutableListOf<TariffBaseline>(item, item)).size, "views")
    }
}
