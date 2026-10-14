package com.acme.transport.safety.coaching.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalCoachingScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalCoachingScheduleStore()
        val item = newGlobalCoachingSchedule("code-467", "label-341")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
