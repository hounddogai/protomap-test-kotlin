package com.acme.transport.customs.inspection.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionScheduleStore()
        val item = newInspectionSchedule("code-920", "label-153")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
