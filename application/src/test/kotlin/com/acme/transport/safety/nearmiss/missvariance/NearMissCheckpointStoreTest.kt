package com.acme.transport.safety.nearmiss.missvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NearMissCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNearMissCheckpointStore()
        val item = newNearMissCheckpoint("code-857", "label-326")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
