package com.acme.transport.safety.coaching.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictCoachingScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictCoachingScheduleStore()
        val item = newStrictCoachingSchedule("code-601", "title-670")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
