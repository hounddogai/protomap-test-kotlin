package com.acme.transport.customs.tariff.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultTariffAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultTariffAgreementStore()
        val item = newDefaultTariffAgreement("code-607", "name-747")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
