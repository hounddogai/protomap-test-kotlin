package com.acme.transport.lastmile.redelivery.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class RedeliveryExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRedeliveryException("code-343", "title-713")
        val view = toRedeliveryExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRedeliveryExceptionViews(mutableListOf<RedeliveryException>(item, item)).size, "views")
    }
}
