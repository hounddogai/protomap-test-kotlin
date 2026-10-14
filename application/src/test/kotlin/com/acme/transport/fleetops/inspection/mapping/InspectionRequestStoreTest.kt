package com.acme.transport.fleetops.inspection.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionRequestStore()
        val item = newInspectionRequest("code-547", "title-378")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
