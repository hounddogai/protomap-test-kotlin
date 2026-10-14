package com.acme.transport.freightpay.audit.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditTargetTest {
    @Test
    fun `starts in planned`() {
        val item = newAuditTarget("code-544", "name-475")
        assertEquals(AuditTargetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newAuditTarget("code-544", "name-475")
        try {
            item.moveTo(AuditTargetStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditTargetStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAuditTarget("code-544", "name-475")
        item.status = AuditTargetStatus.CANCELLED
        assertFails("move") { item.moveTo(AuditTargetStatus.PLANNED) }
    }
}
