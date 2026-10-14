package com.acme.transport.customs.tariff.tariffcycle

import kotlin.test.Test
import kotlin.test.assertEquals

class TariffBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTariffBatch("code-802", "title-161")
        val view = toTariffBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTariffBatchViews(mutableListOf<TariffBatch>(item, item)).size, "views")
    }
}
