package com.acme.transport.fleetops.inspection.inspectionpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionBaselineStore()
        val item = newInspectionBaseline("code-111", "label-69")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
