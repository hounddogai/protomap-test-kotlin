package com.acme.transport.customs.broker.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BrokerExceptionTest {
    @Test
    fun `starts in created`() {
        val item = newBrokerException("code-366", "name-278")
        assertEquals(BrokerExceptionStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newBrokerException("code-366", "name-278")
        try {
            item.moveTo(BrokerExceptionStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BrokerExceptionStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBrokerException("code-366", "name-278")
        item.status = BrokerExceptionStatus.RETIRED
        assertFails("move") { item.moveTo(BrokerExceptionStatus.CREATED) }
    }
}
