package com.acme.transport.terminal.crossdock.cappeddockestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalCrossDockSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalCrossDockSummaryStore()
        val item = newGlobalCrossDockSummary("code-476", "title-622")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
