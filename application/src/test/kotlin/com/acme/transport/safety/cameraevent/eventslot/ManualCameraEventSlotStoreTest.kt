package com.acme.transport.safety.cameraevent.eventslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualCameraEventSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualCameraEventSlotStore()
        val item = newManualCameraEventSlot("code-414", "label-252")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
