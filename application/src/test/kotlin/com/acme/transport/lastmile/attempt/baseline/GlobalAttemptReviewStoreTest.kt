package com.acme.transport.lastmile.attempt.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalAttemptReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalAttemptReviewStore()
        val item = newGlobalAttemptReview("code-316", "name-989")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
