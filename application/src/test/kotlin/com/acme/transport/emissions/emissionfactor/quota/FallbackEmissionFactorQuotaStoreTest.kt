package com.acme.transport.emissions.emissionfactor.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackEmissionFactorQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackEmissionFactorQuotaStore()
        val item = newFallbackEmissionFactorQuota("code-917", "name-596")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
