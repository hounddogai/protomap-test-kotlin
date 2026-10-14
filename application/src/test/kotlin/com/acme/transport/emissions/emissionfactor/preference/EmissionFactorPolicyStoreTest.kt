package com.acme.transport.emissions.emissionfactor.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EmissionFactorPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEmissionFactorPolicyStore()
        val item = newEmissionFactorPolicy("code-930", "label-361")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
