package com.acme.transport.customs.broker.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerBundleTest {
    @Test
    fun `starts in open`() {
        val item = newBrokerBundle("code-594", "name-131")
        assertEquals(BrokerBundleStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newBrokerBundle("code-594", "name-131")
        try {
            item.moveTo(BrokerBundleStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerBundleStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBrokerBundle("code-594", "name-131")
        item.status = BrokerBundleStatus.ARCHIVED
        assertFails("move") { item.moveTo(BrokerBundleStatus.OPEN) }
    }
}
