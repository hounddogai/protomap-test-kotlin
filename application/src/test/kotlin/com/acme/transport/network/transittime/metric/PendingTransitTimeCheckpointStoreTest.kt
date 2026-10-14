package com.acme.transport.network.transittime.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingTransitTimeCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingTransitTimeCheckpointStore()
        val item = newPendingTransitTimeCheckpoint("code-653", "title-925")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
