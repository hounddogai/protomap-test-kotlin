package com.acme.transport.crewing.roster.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RosterMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRosterMetricStore()
        val item = newRosterMetric("code-566", "name-753")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
