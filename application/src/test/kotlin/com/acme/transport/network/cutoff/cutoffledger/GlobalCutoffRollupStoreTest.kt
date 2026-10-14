package com.acme.transport.network.cutoff.cutoffledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalCutoffRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalCutoffRollupStore()
        val item = newGlobalCutoffRollup("code-18", "title-713")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
