package com.acme.transport.freightpay.dispute.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackDisputeRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackDisputeRecordStore()
        val item = newFallbackDisputeRecord("code-221", "name-19")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
