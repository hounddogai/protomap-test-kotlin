package com.acme.transport.terminal.sortation.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingSortationBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingSortationBatchStore()
        val item = newPendingSortationBatch("code-337", "name-826")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
