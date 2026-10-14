package com.acme.transport.terminal.traileryard.yardentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientTrailerYardSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientTrailerYardSegmentStore()
        val item = newLenientTrailerYardSegment("code-492", "label-772")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
