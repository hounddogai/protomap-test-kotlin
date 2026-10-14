package com.acme.transport.emissions.emissionfactor.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EmissionFactorAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEmissionFactorAdjustmentStore()
        val item = newEmissionFactorAdjustment("code-707", "label-818")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
