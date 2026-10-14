package com.acme.transport.fleetops.inspection.inspectionsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalInspectionTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalInspectionTemplateStore()
        val item = newGlobalInspectionTemplate("code-77", "name-919")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
