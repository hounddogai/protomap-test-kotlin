package com.acme.transport.emissions.emissionfactor.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EmissionFactorQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEmissionFactorQuotaStore()
        val item = newEmissionFactorQuota("code-696", "label-296")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
