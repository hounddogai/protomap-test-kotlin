package com.acme.transport.lastmile.drop.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictDropTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictDropTierStore()
        val item = newStrictDropTier("code-305", "title-828")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
