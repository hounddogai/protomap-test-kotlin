package com.acme.transport.terminal.crossdock.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CrossDockTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCrossDockTierStore()
        val item = newCrossDockTier("code-511", "name-675")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
