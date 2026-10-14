package com.acme.transport.terminal.crossdock.dockslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedCrossDockRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedCrossDockRollupStore()
        val item = newCappedCrossDockRollup("code-397", "name-950")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
