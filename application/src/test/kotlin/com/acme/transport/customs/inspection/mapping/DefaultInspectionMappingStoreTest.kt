package com.acme.transport.customs.inspection.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultInspectionMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultInspectionMappingStore()
        val item = newDefaultInspectionMapping("code-762", "label-412")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
