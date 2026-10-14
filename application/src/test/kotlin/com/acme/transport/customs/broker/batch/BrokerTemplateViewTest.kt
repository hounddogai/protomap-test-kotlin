package com.acme.transport.customs.broker.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class BrokerTemplateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBrokerTemplate("code-847", "label-819")
        val view = toBrokerTemplateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBrokerTemplateViews(mutableListOf<BrokerTemplate>(item, item)).size, "views")
    }
}
