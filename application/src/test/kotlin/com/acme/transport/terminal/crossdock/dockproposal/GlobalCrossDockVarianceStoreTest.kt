package com.acme.transport.terminal.crossdock.dockproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalCrossDockVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalCrossDockVarianceStore()
        val item = newGlobalCrossDockVariance("code-194", "title-948")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
