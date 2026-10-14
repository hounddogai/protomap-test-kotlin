package com.acme.transport.emissions.emissionfactor.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedEmissionFactorDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedEmissionFactorDigestStore()
        val item = newBatchedEmissionFactorDigest("code-681", "title-472")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
