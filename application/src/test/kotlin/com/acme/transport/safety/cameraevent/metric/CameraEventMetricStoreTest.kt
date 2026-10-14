package com.acme.transport.safety.cameraevent.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CameraEventMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCameraEventMetricStore()
        val item = newCameraEventMetric("code-649", "name-99")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
