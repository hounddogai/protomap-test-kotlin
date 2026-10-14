package com.acme.transport.safety.violation.primaryviolationquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedViolationAuditTest {
    @Test
    fun `starts in proposed`() {
        val item = newCappedViolationAudit("code-90", "title-262")
        assertEquals(CappedViolationAuditStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newCappedViolationAudit("code-90", "title-262")
        try {
            item.moveTo(CappedViolationAuditStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedViolationAuditStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCappedViolationAudit("code-90", "title-262")
        item.status = CappedViolationAuditStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CappedViolationAuditStatus.PROPOSED) }
    }
}
