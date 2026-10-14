package com.acme.transport.safety.cameraevent.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CameraEventRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCameraEventRollupStore()
        val item = newCameraEventRollup("code-145", "label-952")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
