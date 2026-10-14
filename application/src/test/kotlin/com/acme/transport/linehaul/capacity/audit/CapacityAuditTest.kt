package com.acme.transport.linehaul.capacity.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityAuditTest {
    @Test
    fun `starts in requested`() {
        val item = newCapacityAudit("code-894", "title-903")
        assertEquals(CapacityAuditStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newCapacityAudit("code-894", "title-903")
        try {
            item.moveTo(CapacityAuditStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityAuditStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCapacityAudit("code-894", "title-903")
        item.status = CapacityAuditStatus.EXPIRED
        assertFails("move") { item.moveTo(CapacityAuditStatus.REQUESTED) }
    }
}
