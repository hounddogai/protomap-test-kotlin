package com.acme.transport.safety.cameraevent.eventaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientCameraEventMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientCameraEventMetricStore()
        val item = newLenientCameraEventMetric("code-39", "title-116")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
