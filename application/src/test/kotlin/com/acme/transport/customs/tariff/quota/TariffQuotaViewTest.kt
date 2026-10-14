package com.acme.transport.customs.tariff.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class TariffQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTariffQuota("code-170", "name-118")
        val view = toTariffQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTariffQuotaViews(mutableListOf<TariffQuota>(item, item)).size, "views")
    }
}
