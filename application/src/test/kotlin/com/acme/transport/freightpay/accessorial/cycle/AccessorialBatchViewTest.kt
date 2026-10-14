package com.acme.transport.freightpay.accessorial.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessorialBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAccessorialBatch("code-375", "label-42")
        val view = toAccessorialBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAccessorialBatchViews(mutableListOf<AccessorialBatch>(item, item)).size, "views")
    }
}
