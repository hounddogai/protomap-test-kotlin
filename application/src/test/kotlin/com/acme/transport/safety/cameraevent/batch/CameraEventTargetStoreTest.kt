package com.acme.transport.safety.cameraevent.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CameraEventTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCameraEventTargetStore()
        val item = newCameraEventTarget("code-857", "label-457")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
