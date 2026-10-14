package com.acme.transport.network.transittime.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TransitTimeBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTransitTimeBundleStore()
        val item = newTransitTimeBundle("code-669", "name-550")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
