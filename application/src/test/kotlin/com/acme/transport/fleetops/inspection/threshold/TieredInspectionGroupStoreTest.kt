package com.acme.transport.fleetops.inspection.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredInspectionGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredInspectionGroupStore()
        val item = newTieredInspectionGroup("code-985", "title-822")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
