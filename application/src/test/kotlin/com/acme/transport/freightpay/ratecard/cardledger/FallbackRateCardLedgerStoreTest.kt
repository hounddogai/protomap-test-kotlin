package com.acme.transport.freightpay.ratecard.cardledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackRateCardLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackRateCardLedgerStore()
        val item = newFallbackRateCardLedger("code-154", "title-968")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
