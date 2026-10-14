package com.acme.transport.customs.broker.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerBaselineTest {
    @Test
    fun `starts in idle`() {
        val item = newBrokerBaseline("code-951", "label-356")
        assertEquals(BrokerBaselineStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newBrokerBaseline("code-951", "label-356")
        try {
            item.moveTo(BrokerBaselineStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerBaselineStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newBrokerBaseline("code-951", "label-356")
        item.status = BrokerBaselineStatus.STOPPED
        assertFails("move") { item.moveTo(BrokerBaselineStatus.IDLE) }
    }
}
