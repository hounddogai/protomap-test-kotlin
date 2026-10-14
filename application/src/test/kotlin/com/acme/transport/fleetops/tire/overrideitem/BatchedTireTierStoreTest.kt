package com.acme.transport.fleetops.tire.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedTireTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedTireTierStore()
        val item = newBatchedTireTier("code-163", "name-36")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
