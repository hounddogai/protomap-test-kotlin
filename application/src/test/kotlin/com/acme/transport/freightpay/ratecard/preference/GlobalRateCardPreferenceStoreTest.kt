package com.acme.transport.freightpay.ratecard.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalRateCardPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalRateCardPreferenceStore()
        val item = newGlobalRateCardPreference("code-910", "name-591")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
