package com.acme.transport.safety.cameraevent.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualCameraEventPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualCameraEventPolicyStore()
        val item = newManualCameraEventPolicy("code-809", "name-433")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
