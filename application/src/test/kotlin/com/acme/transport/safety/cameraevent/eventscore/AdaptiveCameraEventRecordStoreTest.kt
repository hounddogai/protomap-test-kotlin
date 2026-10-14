package com.acme.transport.safety.cameraevent.eventscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveCameraEventRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveCameraEventRecordStore()
        val item = newAdaptiveCameraEventRecord("code-481", "title-945")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
