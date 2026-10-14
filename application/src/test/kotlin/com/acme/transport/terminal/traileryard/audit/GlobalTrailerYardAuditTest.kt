package com.acme.transport.terminal.traileryard.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalTrailerYardAuditTest {
    @Test
    fun `starts in planned`() {
        val item = newGlobalTrailerYardAudit("code-567", "name-173")
        assertEquals(GlobalTrailerYardAuditStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newGlobalTrailerYardAudit("code-567", "name-173")
        try {
            item.moveTo(GlobalTrailerYardAuditStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalTrailerYardAuditStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newGlobalTrailerYardAudit("code-567", "name-173")
        item.status = GlobalTrailerYardAuditStatus.CANCELLED
        assertFails("move") { item.moveTo(GlobalTrailerYardAuditStatus.PLANNED) }
    }
}
