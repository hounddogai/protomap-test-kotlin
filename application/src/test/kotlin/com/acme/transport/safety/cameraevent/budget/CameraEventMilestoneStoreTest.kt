package com.acme.transport.safety.cameraevent.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CameraEventMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCameraEventMilestoneStore()
        val item = newCameraEventMilestone("code-986", "title-817")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
