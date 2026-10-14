package com.acme.transport.emissions.emissionfactor.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredEmissionFactorBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredEmissionFactorBatchStore()
        val item = newTieredEmissionFactorBatch("code-921", "label-907")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
