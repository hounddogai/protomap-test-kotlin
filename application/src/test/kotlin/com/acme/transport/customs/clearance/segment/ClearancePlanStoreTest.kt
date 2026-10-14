package com.acme.transport.customs.clearance.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ClearancePlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryClearancePlanStore()
        val item = newClearancePlan("code-864", "label-349")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
