package com.acme.transport.freightpay.dispute.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DisputeRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDisputeRecordStore()
        val item = newDisputeRecord("code-192", "label-763")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
