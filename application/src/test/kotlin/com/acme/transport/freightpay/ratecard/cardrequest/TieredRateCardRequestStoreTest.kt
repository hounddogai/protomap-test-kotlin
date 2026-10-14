package com.acme.transport.freightpay.ratecard.cardrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredRateCardRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredRateCardRequestStore()
        val item = newTieredRateCardRequest("code-395", "name-701")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
