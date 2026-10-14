package com.acme.transport.customs.broker.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class BrokerRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBrokerRecord("code-495", "label-712")
        val view = toBrokerRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBrokerRecordViews(mutableListOf<BrokerRecord>(item, item)).size, "views")
    }
}
