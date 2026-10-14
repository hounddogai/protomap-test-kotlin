package com.acme.transport.linehaul.trailer.trailermilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerScoreStore()
        val item = newTrailerScore("code-983", "label-26")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
