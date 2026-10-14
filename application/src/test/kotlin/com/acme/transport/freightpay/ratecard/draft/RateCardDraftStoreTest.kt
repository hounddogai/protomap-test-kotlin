package com.acme.transport.freightpay.ratecard.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RateCardDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRateCardDraftStore()
        val item = newRateCardDraft("code-49", "label-274")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
