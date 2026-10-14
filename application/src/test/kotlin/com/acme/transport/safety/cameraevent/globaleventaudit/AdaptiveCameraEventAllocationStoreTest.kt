package com.acme.transport.safety.cameraevent.globaleventaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveCameraEventAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveCameraEventAllocationStore()
        val item = newAdaptiveCameraEventAllocation("code-542", "label-919")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
