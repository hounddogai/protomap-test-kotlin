package com.acme.transport.customs.broker.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerRecordTest {
    @Test
    fun `starts in new`() {
        val item = newBrokerRecord("code-495", "label-712")
        assertEquals(BrokerRecordStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newBrokerRecord("code-495", "label-712")
        try {
            item.moveTo(BrokerRecordStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerRecordStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newBrokerRecord("code-495", "label-712")
        item.status = BrokerRecordStatus.FAILED
        assertFails("move") { item.moveTo(BrokerRecordStatus.NEW) }
    }
}
