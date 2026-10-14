package com.acme.transport.freightpay.audit.auditreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictAuditReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictAuditReviewStore()
        val item = newStrictAuditReview("code-561", "label-212")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
