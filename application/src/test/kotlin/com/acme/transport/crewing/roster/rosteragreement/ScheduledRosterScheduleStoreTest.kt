package com.acme.transport.crewing.roster.rosteragreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledRosterScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledRosterScheduleStore()
        val item = newScheduledRosterSchedule("code-148", "label-811")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
