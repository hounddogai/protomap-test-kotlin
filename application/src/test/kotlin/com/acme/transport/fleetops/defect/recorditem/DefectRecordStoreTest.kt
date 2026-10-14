package com.acme.transport.fleetops.defect.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefectRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefectRecordStore()
        val item = newDefectRecord("code-731", "label-534")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
