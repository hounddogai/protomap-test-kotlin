package com.acme.transport.lastmile.attempt.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AttemptReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAttemptReviewStore()
        val item = newAttemptReview("code-831", "name-535")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
