package com.acme.transport.network.zone.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ZoneScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryZoneScheduleStore()
        val item = newZoneSchedule("code-543", "name-806")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
