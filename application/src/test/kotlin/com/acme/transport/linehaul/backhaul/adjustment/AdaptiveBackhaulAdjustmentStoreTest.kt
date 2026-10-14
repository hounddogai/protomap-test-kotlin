package com.acme.transport.linehaul.backhaul.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveBackhaulAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveBackhaulAdjustmentStore()
        val item = newAdaptiveBackhaulAdjustment("code-296", "label-493")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
