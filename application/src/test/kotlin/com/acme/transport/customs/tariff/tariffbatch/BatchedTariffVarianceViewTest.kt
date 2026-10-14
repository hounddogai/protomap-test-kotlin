package com.acme.transport.customs.tariff.tariffbatch

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedTariffVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedTariffVariance("code-44", "title-90")
        val view = toBatchedTariffVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedTariffVarianceViews(mutableListOf<BatchedTariffVariance>(item, item)).size, "views")
    }
}
