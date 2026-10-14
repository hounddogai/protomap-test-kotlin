package com.acme.transport.customs.tariff.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TariffAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTariffAgreementStore()
        val item = newTariffAgreement("code-639", "label-80")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
