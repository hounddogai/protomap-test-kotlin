package com.acme.transport.customs.clearance.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ClearanceVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryClearanceVarianceStore()
        val item = newClearanceVariance("code-643", "label-257")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
