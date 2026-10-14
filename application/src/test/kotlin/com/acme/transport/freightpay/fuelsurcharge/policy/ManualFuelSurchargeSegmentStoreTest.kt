package com.acme.transport.freightpay.fuelsurcharge.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualFuelSurchargeSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualFuelSurchargeSegmentStore()
        val item = newManualFuelSurchargeSegment("code-409", "title-975")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
