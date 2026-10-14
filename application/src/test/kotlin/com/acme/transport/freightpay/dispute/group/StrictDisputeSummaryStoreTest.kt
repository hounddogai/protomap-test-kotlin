package com.acme.transport.freightpay.dispute.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictDisputeSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictDisputeSummaryStore()
        val item = newStrictDisputeSummary("code-646", "name-976")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
