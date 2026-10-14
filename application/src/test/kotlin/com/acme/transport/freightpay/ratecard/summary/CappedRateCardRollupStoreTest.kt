package com.acme.transport.freightpay.ratecard.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedRateCardRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedRateCardRollupStore()
        val item = newCappedRateCardRollup("code-128", "label-380")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
