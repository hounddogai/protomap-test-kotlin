package com.acme.transport.customs.broker.lenientbrokerestimate

import kotlin.test.Test
import kotlin.test.assertEquals

class BrokerClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBrokerClaim("code-758", "name-655")
        val view = toBrokerClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBrokerClaimViews(mutableListOf<BrokerClaim>(item, item)).size, "views")
    }
}
