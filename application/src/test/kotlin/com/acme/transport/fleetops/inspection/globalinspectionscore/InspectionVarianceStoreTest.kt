package com.acme.transport.fleetops.inspection.globalinspectionscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionVarianceStore()
        val item = newInspectionVariance("code-415", "title-666")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
