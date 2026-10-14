package com.acme.transport.terminal.dockdoor.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultDockDoorScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultDockDoorScheduleStore()
        val item = newDefaultDockDoorSchedule("code-545", "name-306")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
