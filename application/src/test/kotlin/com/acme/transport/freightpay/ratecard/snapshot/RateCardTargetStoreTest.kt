package com.acme.transport.freightpay.ratecard.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RateCardTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRateCardTargetStore()
        val item = newRateCardTarget("code-139", "name-527")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
