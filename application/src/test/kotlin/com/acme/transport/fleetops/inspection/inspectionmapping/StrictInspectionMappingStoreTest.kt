package com.acme.transport.fleetops.inspection.inspectionmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictInspectionMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictInspectionMappingStore()
        val item = newStrictInspectionMapping("code-90", "title-178")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
