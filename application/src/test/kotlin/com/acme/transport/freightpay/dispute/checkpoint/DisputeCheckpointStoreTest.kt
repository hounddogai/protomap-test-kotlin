package com.acme.transport.freightpay.dispute.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DisputeCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDisputeCheckpointStore()
        val item = newDisputeCheckpoint("code-935", "label-386")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
