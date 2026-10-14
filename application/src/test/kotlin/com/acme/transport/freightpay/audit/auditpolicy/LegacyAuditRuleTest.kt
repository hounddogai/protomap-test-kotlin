package com.acme.transport.freightpay.audit.auditpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyAuditRuleTest {
    @Test
    fun `starts in proposed`() {
        val item = newLegacyAuditRule("code-864", "name-672")
        assertEquals(LegacyAuditRuleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newLegacyAuditRule("code-864", "name-672")
        try {
            item.moveTo(LegacyAuditRuleStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyAuditRuleStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLegacyAuditRule("code-864", "name-672")
        item.status = LegacyAuditRuleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LegacyAuditRuleStatus.PROPOSED) }
    }
}
