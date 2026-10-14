package com.acme.transport.customs.broker.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedBrokerCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedBrokerCheckpointStore()
        val item = newCappedBrokerCheckpoint("code-46", "name-327")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
