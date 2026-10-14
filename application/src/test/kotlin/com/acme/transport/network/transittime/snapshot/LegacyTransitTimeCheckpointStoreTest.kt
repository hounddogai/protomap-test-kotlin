package com.acme.transport.network.transittime.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyTransitTimeCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyTransitTimeCheckpointStore()
        val item = newLegacyTransitTimeCheckpoint("code-578", "label-773")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
