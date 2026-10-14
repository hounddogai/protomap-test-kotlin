package com.acme.transport.freightpay.ratecard.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingRateCardThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingRateCardThresholdStore()
        val item = newRollingRateCardThreshold("code-114", "label-525")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
