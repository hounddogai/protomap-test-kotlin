package com.acme.transport.terminal.dockdoor.doorsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedDockDoorSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedDockDoorSnapshotStore()
        val item = newCappedDockDoorSnapshot("code-398", "label-279")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
