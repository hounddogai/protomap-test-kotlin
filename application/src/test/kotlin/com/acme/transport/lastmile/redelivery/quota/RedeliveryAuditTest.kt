package com.acme.transport.lastmile.redelivery.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryAuditTest {
    @Test
    fun `starts in open`() {
        val item = newRedeliveryAudit("code-309", "label-740")
        assertEquals(RedeliveryAuditStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newRedeliveryAudit("code-309", "label-740")
        try {
            item.moveTo(RedeliveryAuditStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryAuditStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRedeliveryAudit("code-309", "label-740")
        item.status = RedeliveryAuditStatus.ARCHIVED
        assertFails("move") { item.moveTo(RedeliveryAuditStatus.OPEN) }
    }
}
