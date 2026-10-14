package com.acme.transport.freightpay.audit.auditclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingAuditClaimTest {
    @Test
    fun `starts in new`() {
        val item = newRollingAuditClaim("code-918", "name-259")
        assertEquals(RollingAuditClaimStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newRollingAuditClaim("code-918", "name-259")
        try {
            item.moveTo(RollingAuditClaimStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingAuditClaimStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRollingAuditClaim("code-918", "name-259")
        item.status = RollingAuditClaimStatus.FAILED
        assertFails("move") { item.moveTo(RollingAuditClaimStatus.NEW) }
    }
}
