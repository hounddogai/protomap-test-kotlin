package com.acme.transport.customs.broker.brokerslot

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalBrokerAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalBrokerAgreement("code-797", "label-391")
        val view = toGlobalBrokerAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalBrokerAgreementViews(mutableListOf<GlobalBrokerAgreement>(item, item)).size, "views")
    }
}
