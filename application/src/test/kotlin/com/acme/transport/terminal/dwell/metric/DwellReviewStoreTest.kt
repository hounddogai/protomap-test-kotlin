package com.acme.transport.terminal.dwell.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DwellReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDwellReviewStore()
        val item = newDwellReview("code-977", "title-668")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
