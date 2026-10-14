package com.acme.transport.emissions.emissionfactor.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedEmissionFactorRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedEmissionFactorRequestStore()
        val item = newBatchedEmissionFactorRequest("code-917", "title-874")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
