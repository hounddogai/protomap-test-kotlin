package com.acme.transport.customs.tariff.tariffoverride

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalTariffOverrideViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalTariffOverride("code-71", "name-63")
        val view = toRegionalTariffOverrideView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalTariffOverrideViews(mutableListOf<RegionalTariffOverride>(item, item)).size, "views")
    }
}
