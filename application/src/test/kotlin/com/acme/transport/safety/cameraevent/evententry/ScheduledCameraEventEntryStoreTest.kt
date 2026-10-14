package com.acme.transport.safety.cameraevent.evententry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledCameraEventEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledCameraEventEntryStore()
        val item = newScheduledCameraEventEntry("code-128", "title-181")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
