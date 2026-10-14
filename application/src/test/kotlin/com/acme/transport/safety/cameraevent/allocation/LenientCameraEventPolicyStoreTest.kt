package com.acme.transport.safety.cameraevent.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientCameraEventPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientCameraEventPolicyStore()
        val item = newLenientCameraEventPolicy("code-508", "name-760")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
