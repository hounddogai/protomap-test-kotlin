package com.acme.transport.freightpay.ratecard.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RateCardMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRateCardMappingStore()
        val item = newRateCardMapping("code-94", "name-457")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
