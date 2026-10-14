package com.acme.transport.terminal.crossdock.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredCrossDockAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredCrossDockAdjustmentStore()
        val item = newTieredCrossDockAdjustment("code-415", "name-182")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
