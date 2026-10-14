package com.acme.transport.terminal.crossdock.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientCrossDockScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientCrossDockScheduleStore()
        val item = newLenientCrossDockSchedule("code-61", "name-266")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
