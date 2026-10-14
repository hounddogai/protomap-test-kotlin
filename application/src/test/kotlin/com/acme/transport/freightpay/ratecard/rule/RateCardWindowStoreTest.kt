package com.acme.transport.freightpay.ratecard.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RateCardWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRateCardWindowStore()
        val item = newRateCardWindow("code-248", "name-417")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
