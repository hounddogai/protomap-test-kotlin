package com.acme.transport.terminal.crossdock.dockproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredCrossDockBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredCrossDockBaselineStore()
        val item = newTieredCrossDockBaseline("code-489", "name-213")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
