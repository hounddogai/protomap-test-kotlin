package com.acme.transport.freightpay.accessorial.accessorialadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveAccessorialAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveAccessorialAdjustmentStore()
        val item = newAdaptiveAccessorialAdjustment("code-990", "label-565")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
