package com.acme.transport.crewing.swap.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class SwapRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSwapRecord("code-731", "label-897")
        val view = toSwapRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSwapRecordViews(mutableListOf<SwapRecord>(item, item)).size, "views")
    }
}
