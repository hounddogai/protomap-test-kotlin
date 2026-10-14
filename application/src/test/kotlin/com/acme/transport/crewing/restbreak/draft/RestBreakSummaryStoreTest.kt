package com.acme.transport.crewing.restbreak.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RestBreakSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRestBreakSummaryStore()
        val item = newRestBreakSummary("code-705", "name-506")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
