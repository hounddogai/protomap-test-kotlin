package com.acme.transport.customs.hold.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingHoldSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingHoldSummaryStore()
        val item = newPendingHoldSummary("code-681", "label-4")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
