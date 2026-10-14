package com.acme.transport.customs.broker.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class BrokerDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBrokerDigest("code-307", "label-438")
        val view = toBrokerDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBrokerDigestViews(mutableListOf<BrokerDigest>(item, item)).size, "views")
    }
}
