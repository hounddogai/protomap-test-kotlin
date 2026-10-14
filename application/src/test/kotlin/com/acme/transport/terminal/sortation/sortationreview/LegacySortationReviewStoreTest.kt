package com.acme.transport.terminal.sortation.sortationreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacySortationReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacySortationReviewStore()
        val item = newLegacySortationReview("code-552", "title-935")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
