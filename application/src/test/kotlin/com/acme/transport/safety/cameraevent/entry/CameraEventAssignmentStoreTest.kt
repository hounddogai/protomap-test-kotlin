package com.acme.transport.safety.cameraevent.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CameraEventAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCameraEventAssignmentStore()
        val item = newCameraEventAssignment("code-100", "label-629")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
