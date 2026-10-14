package com.acme.transport.customs.broker.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class BrokerExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBrokerException("code-366", "name-278")
        val view = toBrokerExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBrokerExceptionViews(mutableListOf<BrokerException>(item, item)).size, "views")
    }
}
