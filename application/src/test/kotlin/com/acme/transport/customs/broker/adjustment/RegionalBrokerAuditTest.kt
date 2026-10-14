package com.acme.transport.customs.broker.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalBrokerAuditTest {
    @Test
    fun `starts in draft`() {
        val item = newRegionalBrokerAudit("code-213", "title-148")
        assertEquals(RegionalBrokerAuditStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newRegionalBrokerAudit("code-213", "title-148")
        try {
            item.moveTo(RegionalBrokerAuditStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalBrokerAuditStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRegionalBrokerAudit("code-213", "title-148")
        item.status = RegionalBrokerAuditStatus.CLOSED
        assertFails("move") { item.moveTo(RegionalBrokerAuditStatus.DRAFT) }
    }
}
