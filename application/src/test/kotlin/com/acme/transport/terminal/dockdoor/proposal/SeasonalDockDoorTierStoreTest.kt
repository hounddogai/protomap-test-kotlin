package com.acme.transport.terminal.dockdoor.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalDockDoorTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalDockDoorTierStore()
        val item = newSeasonalDockDoorTier("code-35", "name-664")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
