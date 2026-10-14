package com.acme.transport.crewing.restbreak.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientRestBreakReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientRestBreakReviewStore()
        val item = newLenientRestBreakReview("code-984", "name-761")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
