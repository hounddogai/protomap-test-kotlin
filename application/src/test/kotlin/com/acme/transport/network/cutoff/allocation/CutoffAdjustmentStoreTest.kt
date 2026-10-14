package com.acme.transport.network.cutoff.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CutoffAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCutoffAdjustmentStore()
        val item = newCutoffAdjustment("code-926", "label-389")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
