package com.acme.transport.customs.inspection.inspectionentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedInspectionEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedInspectionEntryStore()
        val item = newCappedInspectionEntry("code-106", "title-259")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
