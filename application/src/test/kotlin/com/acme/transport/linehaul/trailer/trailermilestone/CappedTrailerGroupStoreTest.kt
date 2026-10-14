package com.acme.transport.linehaul.trailer.trailermilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedTrailerGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedTrailerGroupStore()
        val item = newCappedTrailerGroup("code-661", "name-10")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
