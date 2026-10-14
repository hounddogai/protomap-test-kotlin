package com.acme.transport.customs.tariff.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class TariffGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTariffGroup("code-431", "name-21")
        val view = toTariffGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTariffGroupViews(mutableListOf<TariffGroup>(item, item)).size, "views")
    }
}
