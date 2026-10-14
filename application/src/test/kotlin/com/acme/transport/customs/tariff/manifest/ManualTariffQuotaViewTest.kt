package com.acme.transport.customs.tariff.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualTariffQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualTariffQuota("code-974", "title-474")
        val view = toManualTariffQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualTariffQuotaViews(mutableListOf<ManualTariffQuota>(item, item)).size, "views")
    }
}
