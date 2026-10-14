package com.acme.transport.crewing.swap.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SwapSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySwapSnapshotStore()
        val item = newSwapSnapshot("code-795", "title-470")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
