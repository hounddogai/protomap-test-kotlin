package com.acme.transport.emissions.emissionfactor.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EmissionFactorPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEmissionFactorPlanStore()
        val item = newEmissionFactorPlan("code-468", "name-288")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
