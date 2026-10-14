package com.acme.transport.freightpay.audit.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditAdjustmentTest {
    @Test
    fun `starts in idle`() {
        val item = newAuditAdjustment("code-246", "name-447")
        assertEquals(AuditAdjustmentStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newAuditAdjustment("code-246", "name-447")
        try {
            item.moveTo(AuditAdjustmentStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditAdjustmentStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAuditAdjustment("code-246", "name-447")
        item.status = AuditAdjustmentStatus.STOPPED
        assertFails("move") { item.moveTo(AuditAdjustmentStatus.IDLE) }
    }
}
