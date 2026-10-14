package com.acme.transport.freightpay.dispute.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DisputeSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDisputeSnapshotStore()
        val item = newDisputeSnapshot("code-492", "title-338")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
