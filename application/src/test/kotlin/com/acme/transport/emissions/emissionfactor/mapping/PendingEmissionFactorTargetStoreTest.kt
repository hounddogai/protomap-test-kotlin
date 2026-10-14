package com.acme.transport.emissions.emissionfactor.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingEmissionFactorTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingEmissionFactorTargetStore()
        val item = newPendingEmissionFactorTarget("code-826", "name-326")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
