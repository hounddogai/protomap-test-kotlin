package com.acme.transport.network.zone.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalZoneScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalZoneScheduleStore()
        val item = newGlobalZoneSchedule("code-528", "title-843")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
