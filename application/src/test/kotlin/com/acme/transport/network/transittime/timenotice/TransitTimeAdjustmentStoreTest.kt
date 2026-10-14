package com.acme.transport.network.transittime.timenotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TransitTimeAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTransitTimeAdjustmentStore()
        val item = newTransitTimeAdjustment("code-522", "label-370")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
