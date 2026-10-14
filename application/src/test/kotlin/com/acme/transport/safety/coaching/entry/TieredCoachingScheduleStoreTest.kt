package com.acme.transport.safety.coaching.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredCoachingScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredCoachingScheduleStore()
        val item = newTieredCoachingSchedule("code-645", "label-225")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
