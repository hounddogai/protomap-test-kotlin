package com.acme.transport.freightpay.audit.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AuditReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAuditReviewStore()
        val item = newAuditReview("code-170", "name-989")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
