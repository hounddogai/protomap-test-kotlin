package com.acme.transport.emissions.emissionfactor.factorpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingEmissionFactorAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingEmissionFactorAgreementStore()
        val item = newRollingEmissionFactorAgreement("code-859", "label-25")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
