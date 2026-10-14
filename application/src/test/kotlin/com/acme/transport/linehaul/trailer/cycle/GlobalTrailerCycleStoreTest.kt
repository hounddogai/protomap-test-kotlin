package com.acme.transport.linehaul.trailer.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalTrailerCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalTrailerCycleStore()
        val item = newGlobalTrailerCycle("code-374", "name-567")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
