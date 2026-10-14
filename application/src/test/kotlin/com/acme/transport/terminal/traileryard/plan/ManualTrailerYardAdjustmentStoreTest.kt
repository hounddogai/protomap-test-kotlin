package com.acme.transport.terminal.traileryard.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualTrailerYardAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualTrailerYardAdjustmentStore()
        val item = newManualTrailerYardAdjustment("code-180", "name-508")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
