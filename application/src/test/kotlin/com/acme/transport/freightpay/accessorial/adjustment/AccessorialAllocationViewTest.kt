package com.acme.transport.freightpay.accessorial.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessorialAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAccessorialAllocation("code-339", "title-735")
        val view = toAccessorialAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAccessorialAllocationViews(mutableListOf<AccessorialAllocation>(item, item)).size, "views")
    }
}
