package com.acme.transport.emissions.emissionfactor.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EmissionFactorAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEmissionFactorAgreementStore()
        val item = newEmissionFactorAgreement("code-477", "title-65")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
