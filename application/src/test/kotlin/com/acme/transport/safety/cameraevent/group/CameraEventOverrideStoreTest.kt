package com.acme.transport.safety.cameraevent.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CameraEventOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCameraEventOverrideStore()
        val item = newCameraEventOverride("code-554", "name-758")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
