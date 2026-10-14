package com.acme.transport.freightpay.audit.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveAuditBatchTest {
    @Test
    fun `starts in draft`() {
        val item = newAdaptiveAuditBatch("code-558", "name-301")
        assertEquals(AdaptiveAuditBatchStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newAdaptiveAuditBatch("code-558", "name-301")
        try {
            item.moveTo(AdaptiveAuditBatchStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveAuditBatchStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAdaptiveAuditBatch("code-558", "name-301")
        item.status = AdaptiveAuditBatchStatus.CLOSED
        assertFails("move") { item.moveTo(AdaptiveAuditBatchStatus.DRAFT) }
    }
}
