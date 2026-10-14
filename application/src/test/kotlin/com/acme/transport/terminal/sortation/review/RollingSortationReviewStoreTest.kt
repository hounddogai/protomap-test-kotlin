package com.acme.transport.terminal.sortation.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingSortationReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingSortationReviewStore()
        val item = newRollingSortationReview("code-905", "label-819")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
