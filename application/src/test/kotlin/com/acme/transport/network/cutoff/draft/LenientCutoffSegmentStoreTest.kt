package com.acme.transport.network.cutoff.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientCutoffSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientCutoffSegmentStore()
        val item = newLenientCutoffSegment("code-264", "label-23")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
