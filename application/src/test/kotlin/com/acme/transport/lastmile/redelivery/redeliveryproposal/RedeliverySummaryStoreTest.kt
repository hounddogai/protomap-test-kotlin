package com.acme.transport.lastmile.redelivery.redeliveryproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RedeliverySummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRedeliverySummaryStore()
        val item = newRedeliverySummary("code-395", "label-307")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
