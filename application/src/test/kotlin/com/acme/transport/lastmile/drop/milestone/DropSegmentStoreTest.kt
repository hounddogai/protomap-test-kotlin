package com.acme.transport.lastmile.drop.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DropSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDropSegmentStore()
        val item = newDropSegment("code-899", "name-842")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
