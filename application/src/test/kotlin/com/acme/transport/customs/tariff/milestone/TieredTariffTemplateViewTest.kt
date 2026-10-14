package com.acme.transport.customs.tariff.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredTariffTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredTariffTemplate("code-134", "title-510")
        val view = toTieredTariffTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredTariffTemplateViews(mutableListOf<TieredTariffTemplate>(item, item)).size, "views")
    }
}
