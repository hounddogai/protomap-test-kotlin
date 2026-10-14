package com.acme.transport.safety.cameraevent.eventslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackCameraEventSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackCameraEventSnapshotStore()
        val item = newFallbackCameraEventSnapshot("code-134", "label-938")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
