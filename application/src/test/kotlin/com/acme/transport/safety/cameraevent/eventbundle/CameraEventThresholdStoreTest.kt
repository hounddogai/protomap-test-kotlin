package com.acme.transport.safety.cameraevent.eventbundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CameraEventThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCameraEventThresholdStore()
        val item = newCameraEventThreshold("code-450", "label-443")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
