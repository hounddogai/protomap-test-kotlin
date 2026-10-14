package com.acme.transport.customs.inspection.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionGroupStore()
        val item = newInspectionGroup("code-596", "label-484")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
