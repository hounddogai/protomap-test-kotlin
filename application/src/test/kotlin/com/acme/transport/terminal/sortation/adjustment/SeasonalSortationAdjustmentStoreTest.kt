package com.acme.transport.terminal.sortation.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalSortationAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalSortationAdjustmentStore()
        val item = newSeasonalSortationAdjustment("code-883", "name-260")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
