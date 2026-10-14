package com.acme.transport.customs.inspection.inspectiondigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackInspectionRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackInspectionRequestStore()
        val item = newFallbackInspectionRequest("code-898", "label-12")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
