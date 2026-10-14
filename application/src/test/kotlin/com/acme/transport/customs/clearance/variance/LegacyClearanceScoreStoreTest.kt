package com.acme.transport.customs.clearance.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyClearanceScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyClearanceScoreStore()
        val item = newLegacyClearanceScore("code-304", "label-205")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
