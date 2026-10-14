package com.acme.transport.emissions.idling.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultIdlingSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultIdlingSegmentStore()
        val item = newDefaultIdlingSegment("code-469", "title-458")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
