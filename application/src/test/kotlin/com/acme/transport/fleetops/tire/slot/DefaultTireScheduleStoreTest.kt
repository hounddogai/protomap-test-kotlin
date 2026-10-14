package com.acme.transport.fleetops.tire.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultTireScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultTireScheduleStore()
        val item = newDefaultTireSchedule("code-117", "name-275")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
