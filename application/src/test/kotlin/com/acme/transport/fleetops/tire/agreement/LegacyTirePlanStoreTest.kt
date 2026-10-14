package com.acme.transport.fleetops.tire.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyTirePlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyTirePlanStore()
        val item = newLegacyTirePlan("code-590", "title-325")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
