package com.acme.transport.emissions.emissionfactor.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EmissionFactorBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEmissionFactorBatchStore()
        val item = newEmissionFactorBatch("code-832", "label-329")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
