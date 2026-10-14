package com.acme.transport.safety.cameraevent.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CameraEventSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCameraEventSummaryStore()
        val item = newCameraEventSummary("code-285", "title-798")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
