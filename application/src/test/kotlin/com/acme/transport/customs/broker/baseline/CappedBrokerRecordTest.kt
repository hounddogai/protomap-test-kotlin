package com.acme.transport.customs.broker.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedBrokerRecordTest {
    @Test
    fun `starts in draft`() {
        val item = newCappedBrokerRecord("code-717", "title-449")
        assertEquals(CappedBrokerRecordStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newCappedBrokerRecord("code-717", "title-449")
        try {
            item.moveTo(CappedBrokerRecordStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedBrokerRecordStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCappedBrokerRecord("code-717", "title-449")
        item.status = CappedBrokerRecordStatus.CLOSED
        assertFails("move") { item.moveTo(CappedBrokerRecordStatus.DRAFT) }
    }
}
