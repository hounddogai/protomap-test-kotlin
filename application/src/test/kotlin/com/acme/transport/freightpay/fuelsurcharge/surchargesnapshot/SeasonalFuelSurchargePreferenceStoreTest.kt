package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalFuelSurchargePreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalFuelSurchargePreferenceStore()
        val item = newSeasonalFuelSurchargePreference("code-26", "label-335")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
