package com.acme.transport.network.transittime.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredTransitTimeWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredTransitTimeWindowStore()
        val item = newTieredTransitTimeWindow("code-272", "label-22")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
