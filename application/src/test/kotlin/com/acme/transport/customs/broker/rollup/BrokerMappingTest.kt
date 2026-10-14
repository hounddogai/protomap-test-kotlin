package com.acme.transport.customs.broker.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerMappingTest {
    @Test
    fun `starts in idle`() {
        val item = newBrokerMapping("code-636", "title-48")
        assertEquals(BrokerMappingStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newBrokerMapping("code-636", "title-48")
        try {
            item.moveTo(BrokerMappingStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerMappingStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBrokerMapping("code-636", "title-48")
        item.status = BrokerMappingStatus.STOPPED
        assertFails("move") { item.moveTo(BrokerMappingStatus.IDLE) }
    }
}
