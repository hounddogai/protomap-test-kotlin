package com.acme.transport.linehaul.laneplan.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LanePlanScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLanePlanScheduleStore()
        val item = newLanePlanSchedule("code-567", "label-457")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
