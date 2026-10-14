package com.acme.transport.fleetops.inspection.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedInspectionBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedInspectionBaselineStore()
        val item = newWeightedInspectionBaseline("code-144", "label-855")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
