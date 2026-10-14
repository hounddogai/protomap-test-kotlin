package com.acme.transport.terminal.crossdock.docknotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveCrossDockAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveCrossDockAdjustmentStore()
        val item = newAdaptiveCrossDockAdjustment("code-460", "label-690")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
