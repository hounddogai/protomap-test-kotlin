package com.acme.transport.customs.clearance.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ClearanceEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryClearanceEstimateStore()
        val item = newClearanceEstimate("code-774", "label-759")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
