package com.acme.transport.customs.inspection.inspectionoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionCycleStore()
        val item = newInspectionCycle("code-566", "label-444")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
