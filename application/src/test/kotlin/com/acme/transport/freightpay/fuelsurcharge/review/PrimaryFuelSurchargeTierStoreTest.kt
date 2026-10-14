package com.acme.transport.freightpay.fuelsurcharge.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryFuelSurchargeTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryFuelSurchargeTierStore()
        val item = newPrimaryFuelSurchargeTier("code-350", "label-641")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
