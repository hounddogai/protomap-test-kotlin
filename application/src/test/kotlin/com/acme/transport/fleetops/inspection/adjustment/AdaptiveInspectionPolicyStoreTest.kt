package com.acme.transport.fleetops.inspection.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveInspectionPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveInspectionPolicyStore()
        val item = newAdaptiveInspectionPolicy("code-693", "name-880")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
