package com.acme.transport.freightpay.audit.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AuditReviewTest {
    @Test
    fun `starts in new`() {
        val item = newAuditReview("code-170", "name-989")
        assertEquals(AuditReviewStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newAuditReview("code-170", "name-989")
        try {
            item.moveTo(AuditReviewStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AuditReviewStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAuditReview("code-170", "name-989")
        item.status = AuditReviewStatus.FAILED
        assertFails("move") { item.moveTo(AuditReviewStatus.NEW) }
    }
}
