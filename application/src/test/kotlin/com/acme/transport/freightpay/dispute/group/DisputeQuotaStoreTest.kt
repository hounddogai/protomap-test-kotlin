package com.acme.transport.freightpay.dispute.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DisputeQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDisputeQuotaStore()
        val item = newDisputeQuota("code-942", "title-606")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
