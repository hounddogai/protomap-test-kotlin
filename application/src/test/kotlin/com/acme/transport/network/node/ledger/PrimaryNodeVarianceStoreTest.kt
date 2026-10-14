package com.acme.transport.network.node.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryNodeVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryNodeVarianceStore()
        val item = newPrimaryNodeVariance("code-81", "title-838")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
