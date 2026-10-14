package com.acme.transport.freightpay.accessorial.defaultaccessorialadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualAccessorialAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualAccessorialAdjustmentStore()
        val item = newManualAccessorialAdjustment("code-31", "title-345")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
