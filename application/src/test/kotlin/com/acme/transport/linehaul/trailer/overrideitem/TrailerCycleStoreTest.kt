package com.acme.transport.linehaul.trailer.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerCycleStore()
        val item = newTrailerCycle("code-99", "title-364")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
