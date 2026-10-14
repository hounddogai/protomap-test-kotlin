package com.acme.transport.network.zone.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictZoneScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictZoneScheduleStore()
        val item = newStrictZoneSchedule("code-831", "label-123")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
