package com.acme.transport.terminal.traileryard.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerYardAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerYardAdjustmentStore()
        val item = newTrailerYardAdjustment("code-997", "name-342")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
