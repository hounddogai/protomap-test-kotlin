package com.acme.transport.terminal.dwell.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultDwellReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultDwellReviewStore()
        val item = newDefaultDwellReview("code-662", "title-436")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
