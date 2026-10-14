package com.acme.transport.emissions.evcharge.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalEvChargePlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalEvChargePlanStore()
        val item = newGlobalEvChargePlan("code-403", "name-510")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
