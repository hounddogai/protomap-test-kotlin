package com.acme.transport.fleetops.inspection.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalInspectionScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalInspectionScheduleStore()
        val item = newRegionalInspectionSchedule("code-342", "label-99")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
