package com.acme.transport.safety.cameraevent.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CameraEventEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCameraEventEstimateStore()
        val item = newCameraEventEstimate("code-105", "name-558")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
