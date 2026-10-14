package com.acme.transport.emissions.emissionfactor.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EmissionFactorDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEmissionFactorDraftStore()
        val item = newEmissionFactorDraft("code-389", "label-343")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
