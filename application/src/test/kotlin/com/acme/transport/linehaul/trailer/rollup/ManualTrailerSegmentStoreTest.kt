package com.acme.transport.linehaul.trailer.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualTrailerSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualTrailerSegmentStore()
        val item = newManualTrailerSegment("code-757", "label-518")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
