package com.acme.transport.safety.nearmiss.missadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictNearMissScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictNearMissScheduleStore()
        val item = newStrictNearMissSchedule("code-545", "name-797")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
