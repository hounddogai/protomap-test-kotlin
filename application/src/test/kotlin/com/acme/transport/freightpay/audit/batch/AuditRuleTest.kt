package com.acme.transport.freightpay.audit.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditRuleTest {
    @Test
    fun `starts in proposed`() {
        val item = newAuditRule("code-34", "title-928")
        assertEquals(AuditRuleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newAuditRule("code-34", "title-928")
        try {
            item.moveTo(AuditRuleStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditRuleStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAuditRule("code-34", "title-928")
        item.status = AuditRuleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AuditRuleStatus.PROPOSED) }
    }
}
