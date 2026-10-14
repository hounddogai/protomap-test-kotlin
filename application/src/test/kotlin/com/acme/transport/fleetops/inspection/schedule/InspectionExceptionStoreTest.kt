package com.acme.transport.fleetops.inspection.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionExceptionStore()
        val item = newInspectionException("code-801", "name-938")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
