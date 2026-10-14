package com.acme.transport.freightpay.audit.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackAuditTargetTest {
    @Test
    fun `starts in idle`() {
        val item = newFallbackAuditTarget("code-282", "name-751")
        assertEquals(FallbackAuditTargetStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newFallbackAuditTarget("code-282", "name-751")
        try {
            item.moveTo(FallbackAuditTargetStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackAuditTargetStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFallbackAuditTarget("code-282", "name-751")
        item.status = FallbackAuditTargetStatus.STOPPED
        assertFails("move") { item.moveTo(FallbackAuditTargetStatus.IDLE) }
    }
}
