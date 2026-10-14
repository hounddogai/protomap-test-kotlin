package com.acme.transport.freightpay.audit.auditclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedAuditTargetTest {
    @Test
    fun `starts in proposed`() {
        val item = newCappedAuditTarget("code-490", "name-299")
        assertEquals(CappedAuditTargetStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newCappedAuditTarget("code-490", "name-299")
        try {
            item.moveTo(CappedAuditTargetStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedAuditTargetStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCappedAuditTarget("code-490", "name-299")
        item.status = CappedAuditTargetStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CappedAuditTargetStatus.PROPOSED) }
    }
}
