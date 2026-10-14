package com.acme.transport.linehaul.backhaul.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackhaulAuditTest {
    @Test
    fun `starts in proposed`() {
        val item = newBackhaulAudit("code-277", "label-445")
        assertEquals(BackhaulAuditStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newBackhaulAudit("code-277", "label-445")
        try {
            item.moveTo(BackhaulAuditStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BackhaulAuditStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBackhaulAudit("code-277", "label-445")
        item.status = BackhaulAuditStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BackhaulAuditStatus.PROPOSED) }
    }
}
