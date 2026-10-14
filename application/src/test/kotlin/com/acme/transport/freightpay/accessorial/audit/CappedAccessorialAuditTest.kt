package com.acme.transport.freightpay.accessorial.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedAccessorialAuditTest {
    @Test
    fun `starts in draft`() {
        val item = newCappedAccessorialAudit("code-652", "label-501")
        assertEquals(CappedAccessorialAuditStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newCappedAccessorialAudit("code-652", "label-501")
        try {
            item.moveTo(CappedAccessorialAuditStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedAccessorialAuditStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newCappedAccessorialAudit("code-652", "label-501")
        item.status = CappedAccessorialAuditStatus.CLOSED
        assertFails("move") { item.moveTo(CappedAccessorialAuditStatus.DRAFT) }
    }
}
