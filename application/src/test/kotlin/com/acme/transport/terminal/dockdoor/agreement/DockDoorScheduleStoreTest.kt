package com.acme.transport.terminal.dockdoor.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DockDoorScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDockDoorScheduleStore()
        val item = newDockDoorSchedule("code-725", "name-592")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
