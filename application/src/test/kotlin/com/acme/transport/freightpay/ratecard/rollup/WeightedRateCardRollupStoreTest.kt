package com.acme.transport.freightpay.ratecard.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedRateCardRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedRateCardRollupStore()
        val item = newWeightedRateCardRollup("code-687", "name-240")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
