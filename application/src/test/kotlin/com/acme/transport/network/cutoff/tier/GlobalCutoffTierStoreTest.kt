package com.acme.transport.network.cutoff.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalCutoffTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalCutoffTierStore()
        val item = newGlobalCutoffTier("code-832", "name-37")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
