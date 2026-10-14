package com.acme.transport.fleetops.inspection.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveInspectionThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveInspectionThresholdStore()
        val item = newAdaptiveInspectionThreshold("code-281", "name-796")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
