package com.acme.transport.safety.cameraevent.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CameraEventDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCameraEventDraftStore()
        val item = newCameraEventDraft("code-638", "label-928")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
