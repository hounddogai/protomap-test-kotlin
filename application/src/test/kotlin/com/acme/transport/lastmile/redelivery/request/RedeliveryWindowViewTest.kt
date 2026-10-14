package com.acme.transport.lastmile.redelivery.request

import kotlin.test.Test
import kotlin.test.assertEquals

class RedeliveryWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRedeliveryWindow("code-22", "label-155")
        val view = toRedeliveryWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRedeliveryWindowViews(mutableListOf<RedeliveryWindow>(item, item)).size, "views")
    }
}
