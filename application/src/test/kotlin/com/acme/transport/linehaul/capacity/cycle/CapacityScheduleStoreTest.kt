package com.acme.transport.linehaul.capacity.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CapacityScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCapacityScheduleStore()
        val item = newCapacitySchedule("code-335", "name-260")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
