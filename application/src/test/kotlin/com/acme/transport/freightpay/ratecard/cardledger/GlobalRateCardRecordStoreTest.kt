package com.acme.transport.freightpay.ratecard.cardledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalRateCardRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalRateCardRecordStore()
        val item = newGlobalRateCardRecord("code-945", "label-618")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
