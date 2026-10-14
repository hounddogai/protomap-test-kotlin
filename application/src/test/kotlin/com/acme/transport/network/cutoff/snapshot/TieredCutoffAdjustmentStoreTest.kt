package com.acme.transport.network.cutoff.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredCutoffAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredCutoffAdjustmentStore()
        val item = newTieredCutoffAdjustment("code-504", "label-687")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
