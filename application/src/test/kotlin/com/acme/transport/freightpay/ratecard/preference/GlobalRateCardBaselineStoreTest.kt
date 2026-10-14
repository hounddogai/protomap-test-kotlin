package com.acme.transport.freightpay.ratecard.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalRateCardBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalRateCardBaselineStore()
        val item = newGlobalRateCardBaseline("code-385", "label-510")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
