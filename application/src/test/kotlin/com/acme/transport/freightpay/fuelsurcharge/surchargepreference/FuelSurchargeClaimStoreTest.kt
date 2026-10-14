package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelSurchargeClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelSurchargeClaimStore()
        val item = newFuelSurchargeClaim("code-747", "title-616")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
