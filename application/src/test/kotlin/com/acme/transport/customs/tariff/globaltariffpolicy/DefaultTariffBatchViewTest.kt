package com.acme.transport.customs.tariff.globaltariffpolicy

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultTariffBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultTariffBatch("code-833", "title-744")
        val view = toDefaultTariffBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultTariffBatchViews(mutableListOf<DefaultTariffBatch>(item, item)).size, "views")
    }
}
