package com.acme.transport.freightpay.audit.auditreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictAuditReviewTest {
    @Test
    fun `starts in new`() {
        val item = newStrictAuditReview("code-561", "label-212")
        assertEquals(StrictAuditReviewStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newStrictAuditReview("code-561", "label-212")
        try {
            item.moveTo(StrictAuditReviewStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictAuditReviewStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newStrictAuditReview("code-561", "label-212")
        item.status = StrictAuditReviewStatus.FAILED
        assertFails("move") { item.moveTo(StrictAuditReviewStatus.NEW) }
    }
}
