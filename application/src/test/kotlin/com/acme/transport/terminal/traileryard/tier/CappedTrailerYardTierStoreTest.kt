package com.acme.transport.terminal.traileryard.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedTrailerYardTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedTrailerYardTierStore()
        val item = newCappedTrailerYardTier("code-237", "title-441")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
