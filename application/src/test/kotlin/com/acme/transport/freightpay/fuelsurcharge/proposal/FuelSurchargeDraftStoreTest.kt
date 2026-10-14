package com.acme.transport.freightpay.fuelsurcharge.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelSurchargeDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelSurchargeDraftStore()
        val item = newFuelSurchargeDraft("code-740", "label-143")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
