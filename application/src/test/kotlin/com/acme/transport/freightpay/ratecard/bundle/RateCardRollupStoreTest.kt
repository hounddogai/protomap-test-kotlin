package com.acme.transport.freightpay.ratecard.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RateCardRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRateCardRollupStore()
        val item = newRateCardRollup("code-942", "label-936")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
