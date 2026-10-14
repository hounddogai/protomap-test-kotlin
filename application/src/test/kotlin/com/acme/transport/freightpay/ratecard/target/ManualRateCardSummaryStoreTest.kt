package com.acme.transport.freightpay.ratecard.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualRateCardSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualRateCardSummaryStore()
        val item = newManualRateCardSummary("code-353", "label-888")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
