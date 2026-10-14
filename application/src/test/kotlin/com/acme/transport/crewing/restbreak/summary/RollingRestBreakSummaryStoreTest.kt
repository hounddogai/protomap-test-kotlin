package com.acme.transport.crewing.restbreak.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingRestBreakSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingRestBreakSummaryStore()
        val item = newRollingRestBreakSummary("code-443", "name-142")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
