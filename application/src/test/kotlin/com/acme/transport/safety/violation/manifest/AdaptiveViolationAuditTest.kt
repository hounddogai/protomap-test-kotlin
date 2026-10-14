package com.acme.transport.safety.violation.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveViolationAuditTest {
    @Test
    fun `starts in open`() {
        val item = newAdaptiveViolationAudit("code-317", "name-944")
        assertEquals(AdaptiveViolationAuditStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newAdaptiveViolationAudit("code-317", "name-944")
        try {
            item.moveTo(AdaptiveViolationAuditStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveViolationAuditStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newAdaptiveViolationAudit("code-317", "name-944")
        item.status = AdaptiveViolationAuditStatus.ARCHIVED
        assertFails("move") { item.moveTo(AdaptiveViolationAuditStatus.OPEN) }
    }
}
