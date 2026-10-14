package com.acme.transport.fleetops.inspection.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionWindowStore()
        val item = newInspectionWindow("code-516", "name-968")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
