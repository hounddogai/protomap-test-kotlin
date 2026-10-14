package com.acme.transport.network.zone.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualZoneSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualZoneSummaryStore()
        val item = newManualZoneSummary("code-256", "label-441")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
