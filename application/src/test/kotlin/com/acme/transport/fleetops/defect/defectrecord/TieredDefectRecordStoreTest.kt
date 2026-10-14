package com.acme.transport.fleetops.defect.defectrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredDefectRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredDefectRecordStore()
        val item = newTieredDefectRecord("code-326", "title-202")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
