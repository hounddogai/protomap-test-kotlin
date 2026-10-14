package com.acme.transport.freightpay.ratecard.cardgroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyRateCardGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyRateCardGroupStore()
        val item = newLegacyRateCardGroup("code-396", "name-208")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
