package com.acme.transport.freightpay.accessorial.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalAccessorialAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalAccessorialAdjustmentStore()
        val item = newGlobalAccessorialAdjustment("code-422", "label-570")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
