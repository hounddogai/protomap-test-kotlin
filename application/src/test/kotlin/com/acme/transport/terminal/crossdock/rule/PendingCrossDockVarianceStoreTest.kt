package com.acme.transport.terminal.crossdock.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingCrossDockVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingCrossDockVarianceStore()
        val item = newPendingCrossDockVariance("code-365", "name-782")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
