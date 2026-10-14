package com.acme.transport.freightpay.ratecard.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RateCardAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRateCardAgreementStore()
        val item = newRateCardAgreement("code-17", "name-238")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
