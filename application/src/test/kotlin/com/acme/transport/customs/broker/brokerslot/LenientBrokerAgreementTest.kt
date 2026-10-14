package com.acme.transport.customs.broker.brokerslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientBrokerAgreementTest {
    @Test
    fun `starts in requested`() {
        val item = newLenientBrokerAgreement("code-249", "title-500")
        assertEquals(LenientBrokerAgreementStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newLenientBrokerAgreement("code-249", "title-500")
        try {
            item.moveTo(LenientBrokerAgreementStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientBrokerAgreementStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLenientBrokerAgreement("code-249", "title-500")
        item.status = LenientBrokerAgreementStatus.EXPIRED
        assertFails("move") { item.moveTo(LenientBrokerAgreementStatus.REQUESTED) }
    }
}
