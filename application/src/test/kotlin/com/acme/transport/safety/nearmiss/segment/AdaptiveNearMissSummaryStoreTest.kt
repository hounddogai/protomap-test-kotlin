package com.acme.transport.safety.nearmiss.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveNearMissSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveNearMissSummaryStore()
        val item = newAdaptiveNearMissSummary("code-556", "name-568")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
