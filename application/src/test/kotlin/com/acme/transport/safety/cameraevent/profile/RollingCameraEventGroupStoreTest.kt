package com.acme.transport.safety.cameraevent.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingCameraEventGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingCameraEventGroupStore()
        val item = newRollingCameraEventGroup("code-811", "name-650")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
