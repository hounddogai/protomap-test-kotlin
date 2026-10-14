package com.acme.transport.emissions.emissionfactor.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedEmissionFactorManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedEmissionFactorManifestStore()
        val item = newBatchedEmissionFactorManifest("code-583", "label-687")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
