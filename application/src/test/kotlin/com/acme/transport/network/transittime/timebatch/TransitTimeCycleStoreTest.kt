package com.acme.transport.network.transittime.timebatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TransitTimeCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTransitTimeCycleStore()
        val item = newTransitTimeCycle("code-894", "name-548")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
