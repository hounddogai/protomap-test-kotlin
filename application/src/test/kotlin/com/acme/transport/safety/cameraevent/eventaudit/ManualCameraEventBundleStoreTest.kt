package com.acme.transport.safety.cameraevent.eventaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualCameraEventBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualCameraEventBundleStore()
        val item = newManualCameraEventBundle("code-825", "name-503")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
