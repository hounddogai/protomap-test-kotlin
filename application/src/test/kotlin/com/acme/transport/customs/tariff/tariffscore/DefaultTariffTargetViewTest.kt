package com.acme.transport.customs.tariff.tariffscore

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultTariffTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultTariffTarget("code-192", "title-381")
        val view = toDefaultTariffTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultTariffTargetViews(mutableListOf<DefaultTariffTarget>(item, item)).size, "views")
    }
}
