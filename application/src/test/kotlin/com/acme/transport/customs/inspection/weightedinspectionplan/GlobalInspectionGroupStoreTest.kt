package com.acme.transport.customs.inspection.weightedinspectionplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalInspectionGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalInspectionGroupStore()
        val item = newGlobalInspectionGroup("code-824", "label-437")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
