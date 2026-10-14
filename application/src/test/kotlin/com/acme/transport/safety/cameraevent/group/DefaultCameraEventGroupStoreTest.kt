package com.acme.transport.safety.cameraevent.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultCameraEventGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultCameraEventGroupStore()
        val item = newDefaultCameraEventGroup("code-193", "label-833")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
