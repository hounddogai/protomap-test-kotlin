package com.acme.transport.network.zone.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredZoneSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredZoneSummaryStore()
        val item = newTieredZoneSummary("code-103", "title-154")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
