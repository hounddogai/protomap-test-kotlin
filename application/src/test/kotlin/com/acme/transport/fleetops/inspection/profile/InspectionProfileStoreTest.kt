package com.acme.transport.fleetops.inspection.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionProfileStore()
        val item = newInspectionProfile("code-939", "title-162")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
