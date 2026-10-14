package com.acme.transport.freightpay.ratecard.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictRateCardEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictRateCardEntryStore()
        val item = newStrictRateCardEntry("code-536", "title-372")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
