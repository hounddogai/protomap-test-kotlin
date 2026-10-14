package com.acme.transport.customs.inspection.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackInspectionAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackInspectionAdjustmentStore()
        val item = newFallbackInspectionAdjustment("code-293", "name-907")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
