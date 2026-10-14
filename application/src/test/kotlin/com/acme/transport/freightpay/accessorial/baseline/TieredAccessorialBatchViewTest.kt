package com.acme.transport.freightpay.accessorial.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredAccessorialBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredAccessorialBatch("code-208", "title-229")
        val view = toTieredAccessorialBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredAccessorialBatchViews(mutableListOf<TieredAccessorialBatch>(item, item)).size, "views")
    }
}
