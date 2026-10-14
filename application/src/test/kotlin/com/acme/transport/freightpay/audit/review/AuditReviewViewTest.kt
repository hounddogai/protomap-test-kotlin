package com.acme.transport.freightpay.audit.review

import kotlin.test.Test
import kotlin.test.assertEquals

class AuditReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAuditReview("code-170", "name-989")
        val view = toAuditReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAuditReviewViews(mutableListOf<AuditReview>(item, item)).size, "views")
    }
}
