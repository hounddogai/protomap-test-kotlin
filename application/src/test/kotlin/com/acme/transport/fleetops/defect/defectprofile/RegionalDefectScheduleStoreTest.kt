package com.acme.transport.fleetops.defect.defectprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalDefectScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalDefectScheduleStore()
        val item = newRegionalDefectSchedule("code-746", "label-299")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
