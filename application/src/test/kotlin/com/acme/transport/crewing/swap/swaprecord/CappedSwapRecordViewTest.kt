package com.acme.transport.crewing.swap.swaprecord

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedSwapRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedSwapRecord("code-689", "name-49")
        val view = toCappedSwapRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedSwapRecordViews(mutableListOf<CappedSwapRecord>(item, item)).size, "views")
    }
}
