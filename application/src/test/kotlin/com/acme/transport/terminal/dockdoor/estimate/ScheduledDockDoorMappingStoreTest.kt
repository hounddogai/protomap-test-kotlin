package com.acme.transport.terminal.dockdoor.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledDockDoorMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledDockDoorMappingStore()
        val item = newScheduledDockDoorMapping("code-515", "name-743")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
