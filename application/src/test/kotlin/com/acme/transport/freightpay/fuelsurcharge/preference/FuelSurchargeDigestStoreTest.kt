package com.acme.transport.freightpay.fuelsurcharge.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelSurchargeDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelSurchargeDigestStore()
        val item = newFuelSurchargeDigest("code-72", "title-675")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
