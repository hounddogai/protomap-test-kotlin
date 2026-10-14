package com.acme.transport.freightpay.ratecard.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictRateCardThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictRateCardThresholdStore()
        val item = newStrictRateCardThreshold("code-991", "label-413")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
