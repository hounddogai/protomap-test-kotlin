package com.acme.transport.crewing.availability.availabilityexception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AvailabilityScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAvailabilityScheduleStore()
        val item = newAvailabilitySchedule("code-139", "name-634")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
