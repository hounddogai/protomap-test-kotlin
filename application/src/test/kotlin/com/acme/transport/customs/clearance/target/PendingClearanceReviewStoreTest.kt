package com.acme.transport.customs.clearance.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingClearanceReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingClearanceReviewStore()
        val item = newPendingClearanceReview("code-814", "title-473")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
