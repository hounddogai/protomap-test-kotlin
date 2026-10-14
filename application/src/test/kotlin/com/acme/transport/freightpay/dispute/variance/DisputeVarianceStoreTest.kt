package com.acme.transport.freightpay.dispute.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DisputeVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDisputeVarianceStore()
        val item = newDisputeVariance("code-617", "name-670")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
