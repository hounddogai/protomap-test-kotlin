package com.acme.transport.terminal.dwell.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DwellCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDwellCheckpointStore()
        val item = newDwellCheckpoint("code-515", "label-803")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
