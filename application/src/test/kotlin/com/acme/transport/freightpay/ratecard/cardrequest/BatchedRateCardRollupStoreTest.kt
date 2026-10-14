package com.acme.transport.freightpay.ratecard.cardrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedRateCardRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedRateCardRollupStore()
        val item = newBatchedRateCardRollup("code-913", "name-953")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
