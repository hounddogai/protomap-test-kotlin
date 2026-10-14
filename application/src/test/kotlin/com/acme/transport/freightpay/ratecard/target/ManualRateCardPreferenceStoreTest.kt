package com.acme.transport.freightpay.ratecard.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualRateCardPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualRateCardPreferenceStore()
        val item = newManualRateCardPreference("code-695", "label-670")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
