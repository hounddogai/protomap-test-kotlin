package com.acme.transport.freightpay.ratecard.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictRateCardTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictRateCardTargetStore()
        val item = newStrictRateCardTarget("code-439", "title-316")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
