package com.acme.transport.emissions.emissionfactor.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EmissionFactorTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEmissionFactorTemplateStore()
        val item = newEmissionFactorTemplate("code-474", "label-622")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
