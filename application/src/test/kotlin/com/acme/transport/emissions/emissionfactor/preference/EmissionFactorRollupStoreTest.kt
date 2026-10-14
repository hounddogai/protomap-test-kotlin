package com.acme.transport.emissions.emissionfactor.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EmissionFactorRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEmissionFactorRollupStore()
        val item = newEmissionFactorRollup("code-476", "title-232")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
