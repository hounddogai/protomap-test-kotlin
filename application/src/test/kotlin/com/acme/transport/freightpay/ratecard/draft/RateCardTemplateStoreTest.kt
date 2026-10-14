package com.acme.transport.freightpay.ratecard.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RateCardTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRateCardTemplateStore()
        val item = newRateCardTemplate("code-536", "label-30")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
