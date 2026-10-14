package com.acme.transport.safety.cameraevent.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CameraEventAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCameraEventAdjustmentStore()
        val item = newCameraEventAdjustment("code-7", "title-551")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
