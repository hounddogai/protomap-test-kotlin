package com.acme.transport.customs.broker.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BrokerSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBrokerSnapshotStore()
        val item = newBrokerSnapshot("code-701", "label-845")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
