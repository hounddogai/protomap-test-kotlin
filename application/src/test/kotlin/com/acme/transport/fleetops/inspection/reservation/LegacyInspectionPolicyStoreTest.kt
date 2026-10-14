package com.acme.transport.fleetops.inspection.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyInspectionPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyInspectionPolicyStore()
        val item = newLegacyInspectionPolicy("code-52", "name-504")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
