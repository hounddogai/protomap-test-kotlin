package com.acme.transport.customs.broker.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerTemplateTest {
    @Test
    fun `starts in planned`() {
        val item = newBrokerTemplate("code-847", "label-819")
        assertEquals(BrokerTemplateStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newBrokerTemplate("code-847", "label-819")
        try {
            item.moveTo(BrokerTemplateStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerTemplateStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newBrokerTemplate("code-847", "label-819")
        item.status = BrokerTemplateStatus.CANCELLED
        assertFails("move") { item.moveTo(BrokerTemplateStatus.PLANNED) }
    }
}
