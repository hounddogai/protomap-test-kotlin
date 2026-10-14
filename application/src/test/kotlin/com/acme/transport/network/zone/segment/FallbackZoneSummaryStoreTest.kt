package com.acme.transport.network.zone.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackZoneSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackZoneSummaryStore()
        val item = newFallbackZoneSummary("code-648", "title-826")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
