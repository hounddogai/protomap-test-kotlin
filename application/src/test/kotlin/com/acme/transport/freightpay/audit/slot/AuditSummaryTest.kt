package com.acme.transport.freightpay.audit.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditSummaryTest {
    @Test
    fun `starts in proposed`() {
        val item = newAuditSummary("code-343", "name-969")
        assertEquals(AuditSummaryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newAuditSummary("code-343", "name-969")
        try {
            item.moveTo(AuditSummaryStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditSummaryStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAuditSummary("code-343", "name-969")
        item.status = AuditSummaryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AuditSummaryStatus.PROPOSED) }
    }
}
