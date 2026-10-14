package com.acme.transport.freightpay.audit.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditExceptionTest {
    @Test
    fun `starts in idle`() {
        val item = newAuditException("code-720", "name-896")
        assertEquals(AuditExceptionStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newAuditException("code-720", "name-896")
        try {
            item.moveTo(AuditExceptionStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditExceptionStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAuditException("code-720", "name-896")
        item.status = AuditExceptionStatus.STOPPED
        assertFails("move") { item.moveTo(AuditExceptionStatus.IDLE) }
    }
}
