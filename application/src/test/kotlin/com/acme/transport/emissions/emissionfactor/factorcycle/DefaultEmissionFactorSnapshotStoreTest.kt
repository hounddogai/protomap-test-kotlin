package com.acme.transport.emissions.emissionfactor.factorcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultEmissionFactorSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultEmissionFactorSnapshotStore()
        val item = newDefaultEmissionFactorSnapshot("code-446", "name-39")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
