package com.acme.transport.customs.inspection.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionAdjustmentStore()
        val item = newInspectionAdjustment("code-836", "label-333")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
