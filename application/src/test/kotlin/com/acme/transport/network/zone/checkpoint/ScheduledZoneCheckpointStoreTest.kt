package com.acme.transport.network.zone.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledZoneCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledZoneCheckpointStore()
        val item = newScheduledZoneCheckpoint("code-774", "name-381")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
