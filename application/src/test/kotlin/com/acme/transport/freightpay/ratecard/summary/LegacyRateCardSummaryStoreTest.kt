package com.acme.transport.freightpay.ratecard.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyRateCardSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyRateCardSummaryStore()
        val item = newLegacyRateCardSummary("code-6", "name-758")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
