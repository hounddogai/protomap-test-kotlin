package com.acme.transport.emissions.emissionfactor.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EmissionFactorSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEmissionFactorSnapshotStore()
        val item = newEmissionFactorSnapshot("code-456", "name-986")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
