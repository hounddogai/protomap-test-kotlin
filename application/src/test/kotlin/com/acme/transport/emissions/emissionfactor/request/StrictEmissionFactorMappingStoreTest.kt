package com.acme.transport.emissions.emissionfactor.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictEmissionFactorMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictEmissionFactorMappingStore()
        val item = newStrictEmissionFactorMapping("code-136", "title-699")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
