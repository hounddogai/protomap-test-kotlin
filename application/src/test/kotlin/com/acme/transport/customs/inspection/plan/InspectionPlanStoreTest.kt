package com.acme.transport.customs.inspection.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionPlanStore()
        val item = newInspectionPlan("code-87", "label-874")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
