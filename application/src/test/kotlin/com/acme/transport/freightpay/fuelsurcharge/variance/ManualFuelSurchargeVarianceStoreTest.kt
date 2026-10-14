package com.acme.transport.freightpay.fuelsurcharge.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualFuelSurchargeVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualFuelSurchargeVarianceStore()
        val item = newManualFuelSurchargeVariance("code-802", "label-953")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
