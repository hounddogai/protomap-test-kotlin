package com.acme.transport.freightpay.ratecard.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyRateCardTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyRateCardTargetStore()
        val item = newLegacyRateCardTarget("code-322", "label-366")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
