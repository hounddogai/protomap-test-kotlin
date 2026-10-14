package com.acme.transport.freightpay.audit.lenientauditproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditTierTest {
    @Test
    fun `starts in new`() {
        val item = newAuditTier("code-558", "label-398")
        assertEquals(AuditTierStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newAuditTier("code-558", "label-398")
        try {
            item.moveTo(AuditTierStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditTierStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAuditTier("code-558", "label-398")
        item.status = AuditTierStatus.FAILED
        assertFails("move") { item.moveTo(AuditTierStatus.NEW) }
    }
}
