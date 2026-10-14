package com.acme.transport.emissions.emissionfactor.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EmissionFactorRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEmissionFactorRequestStore()
        val item = newEmissionFactorRequest("code-897", "label-664")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
