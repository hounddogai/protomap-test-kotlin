package com.acme.transport.terminal.crossdock.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalCrossDockAuditTest {
    @Test
    fun `starts in planned`() {
        val item = newGlobalCrossDockAudit("code-189", "label-863")
        assertEquals(GlobalCrossDockAuditStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newGlobalCrossDockAudit("code-189", "label-863")
        try {
            item.moveTo(GlobalCrossDockAuditStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalCrossDockAuditStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newGlobalCrossDockAudit("code-189", "label-863")
        item.status = GlobalCrossDockAuditStatus.CANCELLED
        assertFails("move") { item.moveTo(GlobalCrossDockAuditStatus.PLANNED) }
    }
}
