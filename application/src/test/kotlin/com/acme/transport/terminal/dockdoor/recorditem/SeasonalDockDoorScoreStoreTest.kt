package com.acme.transport.terminal.dockdoor.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalDockDoorScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalDockDoorScoreStore()
        val item = newSeasonalDockDoorScore("code-720", "name-595")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
