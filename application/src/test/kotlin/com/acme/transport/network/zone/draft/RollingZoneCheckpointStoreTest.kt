package com.acme.transport.network.zone.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingZoneCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingZoneCheckpointStore()
        val item = newRollingZoneCheckpoint("code-870", "title-634")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
