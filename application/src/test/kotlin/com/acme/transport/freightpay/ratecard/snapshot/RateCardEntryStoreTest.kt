package com.acme.transport.freightpay.ratecard.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RateCardEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRateCardEntryStore()
        val item = newRateCardEntry("code-765", "name-179")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
