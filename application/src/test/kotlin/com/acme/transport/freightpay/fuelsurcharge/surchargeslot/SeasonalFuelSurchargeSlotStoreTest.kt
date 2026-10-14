package com.acme.transport.freightpay.fuelsurcharge.surchargeslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalFuelSurchargeSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalFuelSurchargeSlotStore()
        val item = newSeasonalFuelSurchargeSlot("code-757", "title-272")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
