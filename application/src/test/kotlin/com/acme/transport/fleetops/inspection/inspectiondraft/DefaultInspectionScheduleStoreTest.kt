package com.acme.transport.fleetops.inspection.inspectiondraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultInspectionScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultInspectionScheduleStore()
        val item = newDefaultInspectionSchedule("code-259", "name-34")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
