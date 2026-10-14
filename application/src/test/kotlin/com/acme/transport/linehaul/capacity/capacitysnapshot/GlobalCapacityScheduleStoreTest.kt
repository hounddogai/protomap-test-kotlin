package com.acme.transport.linehaul.capacity.capacitysnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalCapacityScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalCapacityScheduleStore()
        val item = newGlobalCapacitySchedule("code-986", "label-81")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
