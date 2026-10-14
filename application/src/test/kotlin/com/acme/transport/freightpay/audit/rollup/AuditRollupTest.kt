package com.acme.transport.freightpay.audit.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditRollupTest {
    @Test
    fun `starts in planned`() {
        val item = newAuditRollup("code-618", "title-505")
        assertEquals(AuditRollupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newAuditRollup("code-618", "title-505")
        try {
            item.moveTo(AuditRollupStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditRollupStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAuditRollup("code-618", "title-505")
        item.status = AuditRollupStatus.CANCELLED
        assertFails("move") { item.moveTo(AuditRollupStatus.PLANNED) }
    }
}
