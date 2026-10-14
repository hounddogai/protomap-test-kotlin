package com.acme.transport.fleetops.tire.tirebudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualTireBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualTireBudgetStore()
        val item = newManualTireBudget("code-533", "name-413")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
