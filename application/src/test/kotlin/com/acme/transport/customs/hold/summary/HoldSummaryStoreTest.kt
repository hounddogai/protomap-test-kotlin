package com.acme.transport.customs.hold.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HoldSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHoldSummaryStore()
        val item = newHoldSummary("code-49", "name-451")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
