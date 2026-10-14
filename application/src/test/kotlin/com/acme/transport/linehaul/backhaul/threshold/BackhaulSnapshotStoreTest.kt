package com.acme.transport.linehaul.backhaul.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BackhaulSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBackhaulSnapshotStore()
        val item = newBackhaulSnapshot("code-807", "name-818")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
