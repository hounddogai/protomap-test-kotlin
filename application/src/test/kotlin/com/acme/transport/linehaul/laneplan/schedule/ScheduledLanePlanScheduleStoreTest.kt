package com.acme.transport.linehaul.laneplan.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledLanePlanScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledLanePlanScheduleStore()
        val item = newScheduledLanePlanSchedule("code-235", "label-442")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
