package com.acme.transport.emissions.emissionfactor.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EmissionFactorCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEmissionFactorCheckpointStore()
        val item = newEmissionFactorCheckpoint("code-469", "label-922")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
