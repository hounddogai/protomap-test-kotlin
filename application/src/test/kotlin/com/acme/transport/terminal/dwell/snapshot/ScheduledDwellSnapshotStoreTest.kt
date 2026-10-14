package com.acme.transport.terminal.dwell.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledDwellSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledDwellSnapshotStore()
        val item = newScheduledDwellSnapshot("code-292", "label-531")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
