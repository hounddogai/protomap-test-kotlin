package com.acme.transport.customs.inspection.inspectionestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyInspectionEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyInspectionEstimateStore()
        val item = newLegacyInspectionEstimate("code-460", "label-407")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
