package com.acme.transport.customs.inspection.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionMappingStore()
        val item = newInspectionMapping("code-182", "label-893")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
