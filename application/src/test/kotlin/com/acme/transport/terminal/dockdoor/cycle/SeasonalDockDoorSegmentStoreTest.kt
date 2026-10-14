package com.acme.transport.terminal.dockdoor.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalDockDoorSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalDockDoorSegmentStore()
        val item = newSeasonalDockDoorSegment("code-965", "title-257")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
