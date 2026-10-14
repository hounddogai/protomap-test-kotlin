package com.acme.transport.freightpay.audit.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditOverrideTest {
    @Test
    fun `starts in draft`() {
        val item = newAuditOverride("code-301", "label-792")
        assertEquals(AuditOverrideStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newAuditOverride("code-301", "label-792")
        try {
            item.moveTo(AuditOverrideStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditOverrideStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAuditOverride("code-301", "label-792")
        item.status = AuditOverrideStatus.CLOSED
        assertFails("move") { item.moveTo(AuditOverrideStatus.DRAFT) }
    }
}
