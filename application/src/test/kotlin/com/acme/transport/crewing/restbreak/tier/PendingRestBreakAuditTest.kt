package com.acme.transport.crewing.restbreak.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingRestBreakAuditTest {
    @Test
    fun `starts in idle`() {
        val item = newPendingRestBreakAudit("code-279", "title-908")
        assertEquals(PendingRestBreakAuditStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newPendingRestBreakAudit("code-279", "title-908")
        try {
            item.moveTo(PendingRestBreakAuditStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingRestBreakAuditStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPendingRestBreakAudit("code-279", "title-908")
        item.status = PendingRestBreakAuditStatus.STOPPED
        assertFails("move") { item.moveTo(PendingRestBreakAuditStatus.IDLE) }
    }
}
