package com.acme.transport.customs.broker.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedBrokerMappingTest {
    @Test
    fun `starts in proposed`() {
        val item = newCappedBrokerMapping("code-305", "label-643")
        assertEquals(CappedBrokerMappingStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newCappedBrokerMapping("code-305", "label-643")
        try {
            item.moveTo(CappedBrokerMappingStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedBrokerMappingStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCappedBrokerMapping("code-305", "label-643")
        item.status = CappedBrokerMappingStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CappedBrokerMappingStatus.PROPOSED) }
    }
}
