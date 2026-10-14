package com.acme.transport.fleetops.inspection.inspectionagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionRecordStore()
        val item = newInspectionRecord("code-715", "name-780")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
