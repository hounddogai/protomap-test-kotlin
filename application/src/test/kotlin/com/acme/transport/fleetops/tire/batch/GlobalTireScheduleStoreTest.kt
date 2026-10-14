package com.acme.transport.fleetops.tire.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalTireScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalTireScheduleStore()
        val item = newGlobalTireSchedule("code-630", "name-328")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
