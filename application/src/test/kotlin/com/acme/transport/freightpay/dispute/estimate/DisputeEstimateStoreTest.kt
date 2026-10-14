package com.acme.transport.freightpay.dispute.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DisputeEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDisputeEstimateStore()
        val item = newDisputeEstimate("code-892", "label-638")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
