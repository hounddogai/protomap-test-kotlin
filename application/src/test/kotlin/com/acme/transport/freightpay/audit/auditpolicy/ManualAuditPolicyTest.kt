package com.acme.transport.freightpay.audit.auditpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualAuditPolicyTest {
    @Test
    fun `starts in open`() {
        val item = newManualAuditPolicy("code-617", "title-123")
        assertEquals(ManualAuditPolicyStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newManualAuditPolicy("code-617", "title-123")
        try {
            item.moveTo(ManualAuditPolicyStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualAuditPolicyStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newManualAuditPolicy("code-617", "title-123")
        item.status = ManualAuditPolicyStatus.ARCHIVED
        assertFails("move") { item.moveTo(ManualAuditPolicyStatus.OPEN) }
    }
}
