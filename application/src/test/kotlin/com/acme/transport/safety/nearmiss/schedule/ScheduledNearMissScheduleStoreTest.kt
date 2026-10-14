package com.acme.transport.safety.nearmiss.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledNearMissScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledNearMissScheduleStore()
        val item = newScheduledNearMissSchedule("code-320", "label-579")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
