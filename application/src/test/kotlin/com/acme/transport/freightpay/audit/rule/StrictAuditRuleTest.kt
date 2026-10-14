package com.acme.transport.freightpay.audit.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictAuditRuleTest {
    @Test
    fun `starts in proposed`() {
        val item = newStrictAuditRule("code-925", "label-459")
        assertEquals(StrictAuditRuleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newStrictAuditRule("code-925", "label-459")
        try {
            item.moveTo(StrictAuditRuleStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictAuditRuleStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newStrictAuditRule("code-925", "label-459")
        item.status = StrictAuditRuleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(StrictAuditRuleStatus.PROPOSED) }
    }
}
