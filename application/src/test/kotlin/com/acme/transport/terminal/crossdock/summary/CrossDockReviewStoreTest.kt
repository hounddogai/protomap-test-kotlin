package com.acme.transport.terminal.crossdock.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CrossDockReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCrossDockReviewStore()
        val item = newCrossDockReview("code-157", "title-799")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
