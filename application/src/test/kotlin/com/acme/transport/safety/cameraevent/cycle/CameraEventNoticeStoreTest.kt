package com.acme.transport.safety.cameraevent.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CameraEventNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCameraEventNoticeStore()
        val item = newCameraEventNotice("code-545", "label-852")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
