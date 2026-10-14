package com.acme.transport.customs.tariff.tariffagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingTariffAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingTariffAgreementStore()
        val item = newPendingTariffAgreement("code-336", "title-185")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
