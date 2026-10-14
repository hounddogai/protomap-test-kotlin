package com.acme.transport.freightpay.accessorial.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingAccessorialGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingAccessorialGroup("code-752", "name-333")
        val view = toPendingAccessorialGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingAccessorialGroupViews(mutableListOf<PendingAccessorialGroup>(item, item)).size, "views")
    }
}
