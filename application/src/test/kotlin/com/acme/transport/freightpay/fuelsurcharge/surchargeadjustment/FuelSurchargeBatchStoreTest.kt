package com.acme.transport.freightpay.fuelsurcharge.surchargeadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelSurchargeBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelSurchargeBatchStore()
        val item = newFuelSurchargeBatch("code-36", "name-704")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
