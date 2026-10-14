package com.acme.transport.terminal.sortation.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictSortationTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictSortationTierStore()
        val item = newStrictSortationTier("code-729", "title-288")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
