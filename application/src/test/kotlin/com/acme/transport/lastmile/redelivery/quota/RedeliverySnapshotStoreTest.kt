package com.acme.transport.lastmile.redelivery.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RedeliverySnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRedeliverySnapshotStore()
        val item = newRedeliverySnapshot("code-396", "label-431")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
