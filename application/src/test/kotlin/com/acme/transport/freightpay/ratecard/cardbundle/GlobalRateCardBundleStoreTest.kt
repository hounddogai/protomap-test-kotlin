package com.acme.transport.freightpay.ratecard.cardbundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalRateCardBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalRateCardBundleStore()
        val item = newGlobalRateCardBundle("code-186", "title-322")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
