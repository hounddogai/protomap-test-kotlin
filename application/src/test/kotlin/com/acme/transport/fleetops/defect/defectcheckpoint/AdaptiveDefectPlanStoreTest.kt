package com.acme.transport.fleetops.defect.defectcheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveDefectPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveDefectPlanStore()
        val item = newAdaptiveDefectPlan("code-242", "name-357")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
