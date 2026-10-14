package com.acme.transport.safety.cameraevent.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CameraEventRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCameraEventRequestStore()
        val item = newCameraEventRequest("code-760", "label-540")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
