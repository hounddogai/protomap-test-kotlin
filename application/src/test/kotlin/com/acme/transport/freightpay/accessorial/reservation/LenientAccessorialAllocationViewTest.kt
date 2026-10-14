package com.acme.transport.freightpay.accessorial.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientAccessorialAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientAccessorialAllocation("code-572", "name-282")
        val view = toLenientAccessorialAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientAccessorialAllocationViews(mutableListOf<LenientAccessorialAllocation>(item, item)).size, "views")
    }
}
