package com.acme.transport.freightpay.fuelsurcharge.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalFuelSurchargeExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalFuelSurchargeExceptionStore()
        val item = newGlobalFuelSurchargeException("code-252", "name-663")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
