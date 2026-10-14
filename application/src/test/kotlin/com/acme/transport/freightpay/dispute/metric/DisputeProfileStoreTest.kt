package com.acme.transport.freightpay.dispute.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DisputeProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDisputeProfileStore()
        val item = newDisputeProfile("code-679", "title-644")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
