package com.acme.transport.crewing.restbreak.breaksummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalRestBreakEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalRestBreakEstimateStore()
        val item = newSeasonalRestBreakEstimate("code-18", "name-799")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
