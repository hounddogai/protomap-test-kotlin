package com.acme.transport.safety.cameraevent.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CameraEventGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCameraEventGroupStore()
        val item = newCameraEventGroup("code-321", "name-806")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
