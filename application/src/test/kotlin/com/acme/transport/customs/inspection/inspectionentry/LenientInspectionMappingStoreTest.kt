package com.acme.transport.customs.inspection.inspectionentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientInspectionMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientInspectionMappingStore()
        val item = newLenientInspectionMapping("code-174", "title-709")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
