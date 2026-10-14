package com.acme.transport.customs.tariff.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedTariffSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedTariffSegment("code-544", "name-923")
        val view = toBatchedTariffSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedTariffSegmentViews(mutableListOf<BatchedTariffSegment>(item, item)).size, "views")
    }
}
