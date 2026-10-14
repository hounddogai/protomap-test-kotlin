package com.acme.transport.customs.tariff.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class TariffTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTariffTemplate("code-57", "name-38")
        val view = toTariffTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTariffTemplateViews(mutableListOf<TariffTemplate>(item, item)).size, "views")
    }
}
