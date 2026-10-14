package com.acme.transport.customs.clearance.clearancethreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualClearancePlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualClearancePlanStore()
        val item = newManualClearancePlan("code-360", "name-532")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
