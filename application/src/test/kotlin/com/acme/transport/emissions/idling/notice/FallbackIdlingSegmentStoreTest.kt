package com.acme.transport.emissions.idling.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackIdlingSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackIdlingSegmentStore()
        val item = newFallbackIdlingSegment("code-743", "title-233")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
