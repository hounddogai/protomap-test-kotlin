package com.acme.transport.crewing.restbreak.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RestBreakReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRestBreakReviewStore()
        val item = newRestBreakReview("code-803", "title-577")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
