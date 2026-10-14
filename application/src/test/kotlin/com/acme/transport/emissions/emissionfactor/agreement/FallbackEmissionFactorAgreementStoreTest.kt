package com.acme.transport.emissions.emissionfactor.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackEmissionFactorAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackEmissionFactorAgreementStore()
        val item = newFallbackEmissionFactorAgreement("code-130", "title-329")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
