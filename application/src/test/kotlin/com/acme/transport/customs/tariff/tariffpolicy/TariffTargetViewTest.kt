package com.acme.transport.customs.tariff.tariffpolicy

import kotlin.test.Test
import kotlin.test.assertEquals

class TariffTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTariffTarget("code-598", "title-443")
        val view = toTariffTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTariffTargetViews(mutableListOf<TariffTarget>(item, item)).size, "views")
    }
}
