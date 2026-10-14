package com.acme.transport.terminal.sortation.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SortationCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySortationCheckpointStore()
        val item = newSortationCheckpoint("code-229", "name-709")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
