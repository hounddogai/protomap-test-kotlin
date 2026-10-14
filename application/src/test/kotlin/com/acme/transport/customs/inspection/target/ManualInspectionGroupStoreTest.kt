package com.acme.transport.customs.inspection.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualInspectionGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualInspectionGroupStore()
        val item = newManualInspectionGroup("code-394", "title-419")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
