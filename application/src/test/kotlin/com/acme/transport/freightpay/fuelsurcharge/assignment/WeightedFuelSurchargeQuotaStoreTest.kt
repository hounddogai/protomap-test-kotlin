package com.acme.transport.freightpay.fuelsurcharge.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedFuelSurchargeQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedFuelSurchargeQuotaStore()
        val item = newWeightedFuelSurchargeQuota("code-766", "name-782")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
