package com.acme.transport.fleetops.inspection.pendinginspectionmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalInspectionMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalInspectionMappingStore()
        val item = newGlobalInspectionMapping("code-524", "name-275")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
