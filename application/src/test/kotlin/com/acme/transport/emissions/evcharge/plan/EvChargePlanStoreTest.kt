package com.acme.transport.emissions.evcharge.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EvChargePlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEvChargePlanStore()
        val item = newEvChargePlan("code-515", "title-634")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
