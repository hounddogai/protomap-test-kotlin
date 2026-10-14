package com.acme.transport.lastmile.redelivery.redeliveryentry

import kotlin.test.Test
import kotlin.test.assertEquals

class RedeliveryProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRedeliveryProfile("code-273", "name-26")
        val view = toRedeliveryProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRedeliveryProfileViews(mutableListOf<RedeliveryProfile>(item, item)).size, "views")
    }
}
