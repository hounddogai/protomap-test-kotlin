package com.acme.transport.freightpay.ratecard.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedRateCardWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedRateCardWindowStore()
        val item = newCappedRateCardWindow("code-86", "title-71")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
