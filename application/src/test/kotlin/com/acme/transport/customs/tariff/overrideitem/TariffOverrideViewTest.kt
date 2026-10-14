package com.acme.transport.customs.tariff.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class TariffOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTariffOverride("code-151", "title-434")
        val view = toTariffOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTariffOverrideViews(mutableListOf<TariffOverride>(item, item)).size, "views")
    }
}
