package com.acme.transport.terminal.crossdock.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveCrossDockThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveCrossDockThresholdStore()
        val item = newAdaptiveCrossDockThreshold("code-192", "label-109")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
