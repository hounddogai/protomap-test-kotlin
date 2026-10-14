package com.acme.transport.safety.cameraevent.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualCameraEventSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualCameraEventSummaryStore()
        val item = newManualCameraEventSummary("code-169", "name-164")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
