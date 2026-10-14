package com.acme.transport.freightpay.audit.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingAuditLedgerTest {
    @Test
    fun `starts in proposed`() {
        val item = newRollingAuditLedger("code-5", "name-819")
        assertEquals(RollingAuditLedgerStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRollingAuditLedger("code-5", "name-819")
        try {
            item.moveTo(RollingAuditLedgerStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingAuditLedgerStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRollingAuditLedger("code-5", "name-819")
        item.status = RollingAuditLedgerStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RollingAuditLedgerStatus.PROPOSED) }
    }
}
