package com.acme.transport.network.transittime.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalTransitTimeEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalTransitTimeEntryStore()
        val item = newSeasonalTransitTimeEntry("code-628", "label-682")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
