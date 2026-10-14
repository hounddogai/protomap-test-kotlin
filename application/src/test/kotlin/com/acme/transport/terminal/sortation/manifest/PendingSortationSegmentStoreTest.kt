package com.acme.transport.terminal.sortation.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingSortationSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingSortationSegmentStore()
        val item = newPendingSortationSegment("code-402", "name-193")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
