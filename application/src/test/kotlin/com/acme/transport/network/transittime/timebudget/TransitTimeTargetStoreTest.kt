package com.acme.transport.network.transittime.timebudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TransitTimeTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTransitTimeTargetStore()
        val item = newTransitTimeTarget("code-949", "label-159")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
