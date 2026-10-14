package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalFuelSurchargeAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalFuelSurchargeAdjustmentStore()
        val item = newSeasonalFuelSurchargeAdjustment("code-882", "title-446")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
