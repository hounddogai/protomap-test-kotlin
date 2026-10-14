package com.acme.transport.safety.cameraevent.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CameraEventEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCameraEventEntryStore()
        val item = newCameraEventEntry("code-507", "name-84")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
