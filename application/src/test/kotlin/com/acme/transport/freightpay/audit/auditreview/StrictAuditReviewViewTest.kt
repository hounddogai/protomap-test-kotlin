package com.acme.transport.freightpay.audit.auditreview

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictAuditReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictAuditReview("code-561", "label-212")
        val view = toStrictAuditReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictAuditReviewViews(mutableListOf<StrictAuditReview>(item, item)).size, "views")
    }
}
