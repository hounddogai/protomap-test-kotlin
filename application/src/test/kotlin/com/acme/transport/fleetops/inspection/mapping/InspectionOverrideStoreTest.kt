package com.acme.transport.fleetops.inspection.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionOverrideStore()
        val item = newInspectionOverride("code-778", "name-226")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
