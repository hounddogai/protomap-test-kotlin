package com.acme.transport.emissions.emissionfactor.factorproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackEmissionFactorBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackEmissionFactorBatchStore()
        val item = newFallbackEmissionFactorBatch("code-61", "name-785")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
