package com.acme.transport.customs.inspection.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedInspectionSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedInspectionSlotStore()
        val item = newCappedInspectionSlot("code-991", "name-144")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
