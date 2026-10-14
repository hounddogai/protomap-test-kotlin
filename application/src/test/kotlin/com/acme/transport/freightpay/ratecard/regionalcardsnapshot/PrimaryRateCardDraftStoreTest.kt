package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryRateCardDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryRateCardDraftStore()
        val item = newPrimaryRateCardDraft("code-817", "name-863")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
