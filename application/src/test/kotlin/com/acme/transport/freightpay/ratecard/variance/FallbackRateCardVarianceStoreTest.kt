package com.acme.transport.freightpay.ratecard.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackRateCardVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackRateCardVarianceStore()
        val item = newFallbackRateCardVariance("code-643", "title-395")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
