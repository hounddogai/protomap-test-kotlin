package com.acme.transport.freightpay.fuelsurcharge.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedFuelSurchargeRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedFuelSurchargeRequestStore()
        val item = newBatchedFuelSurchargeRequest("code-984", "title-349")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
