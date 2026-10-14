package com.acme.transport.crewing.swap.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalSwapSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalSwapSegmentStore()
        val item = newSeasonalSwapSegment("code-42", "name-43")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
