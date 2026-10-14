package com.acme.transport.emissions.emissionfactor.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EmissionFactorBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEmissionFactorBudgetStore()
        val item = newEmissionFactorBudget("code-340", "name-671")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
