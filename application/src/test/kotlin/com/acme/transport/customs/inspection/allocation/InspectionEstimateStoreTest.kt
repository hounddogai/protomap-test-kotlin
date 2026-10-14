package com.acme.transport.customs.inspection.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionEstimateStore()
        val item = newInspectionEstimate("code-405", "title-178")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
