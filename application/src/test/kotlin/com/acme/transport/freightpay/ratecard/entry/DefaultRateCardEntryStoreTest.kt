package com.acme.transport.freightpay.ratecard.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultRateCardEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultRateCardEntryStore()
        val item = newDefaultRateCardEntry("code-546", "title-386")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
