package com.acme.transport.safety.cameraevent.eventrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackCameraEventSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackCameraEventSummaryStore()
        val item = newFallbackCameraEventSummary("code-185", "label-74")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
