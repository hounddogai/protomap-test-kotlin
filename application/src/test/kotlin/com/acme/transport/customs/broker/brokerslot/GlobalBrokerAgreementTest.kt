package com.acme.transport.customs.broker.brokerslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalBrokerAgreementTest {
    @Test
    fun `starts in idle`() {
        val item = newGlobalBrokerAgreement("code-797", "label-391")
        assertEquals(GlobalBrokerAgreementStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newGlobalBrokerAgreement("code-797", "label-391")
        try {
            item.moveTo(GlobalBrokerAgreementStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalBrokerAgreementStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newGlobalBrokerAgreement("code-797", "label-391")
        item.status = GlobalBrokerAgreementStatus.STOPPED
        assertFails("move") { item.moveTo(GlobalBrokerAgreementStatus.IDLE) }
    }
}
