package com.acme.transport.emissions.fuelburn.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientFuelBurnBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientFuelBurnBudgetStore()
        val item = newLenientFuelBurnBudget("code-415", "name-429")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
