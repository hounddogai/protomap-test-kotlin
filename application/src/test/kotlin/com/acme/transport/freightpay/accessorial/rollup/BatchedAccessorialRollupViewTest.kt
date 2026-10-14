package com.acme.transport.freightpay.accessorial.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedAccessorialRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedAccessorialRollup("code-246", "title-137")
        val view = toBatchedAccessorialRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedAccessorialRollupViews(mutableListOf<BatchedAccessorialRollup>(item, item)).size, "views")
    }
}
