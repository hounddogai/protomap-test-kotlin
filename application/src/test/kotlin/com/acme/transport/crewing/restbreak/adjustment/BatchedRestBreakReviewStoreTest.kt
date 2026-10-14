package com.acme.transport.crewing.restbreak.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedRestBreakReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedRestBreakReviewStore()
        val item = newBatchedRestBreakReview("code-508", "label-777")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
