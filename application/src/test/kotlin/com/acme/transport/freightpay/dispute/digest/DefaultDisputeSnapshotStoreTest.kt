package com.acme.transport.freightpay.dispute.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultDisputeSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultDisputeSnapshotStore()
        val item = newDefaultDisputeSnapshot("code-550", "label-873")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
