package com.acme.transport.network.hub.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredHubBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredHubBaselineStore()
        val item = newTieredHubBaseline("code-730", "name-26")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
