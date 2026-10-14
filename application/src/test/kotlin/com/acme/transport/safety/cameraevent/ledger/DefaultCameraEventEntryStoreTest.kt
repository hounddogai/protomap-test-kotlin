package com.acme.transport.safety.cameraevent.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultCameraEventEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultCameraEventEntryStore()
        val item = newDefaultCameraEventEntry("code-485", "label-530")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
