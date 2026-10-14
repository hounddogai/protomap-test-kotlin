package com.acme.transport.customs.inspection.inspectionmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionThresholdStore()
        val item = newInspectionThreshold("code-959", "name-980")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
