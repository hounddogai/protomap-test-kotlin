package com.acme.transport.fleetops.inspection.pendinginspectionmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryInspectionPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryInspectionPlanStore()
        val item = newPrimaryInspectionPlan("code-596", "label-33")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
