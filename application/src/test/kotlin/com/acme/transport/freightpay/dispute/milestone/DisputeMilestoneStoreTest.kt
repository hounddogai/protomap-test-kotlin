package com.acme.transport.freightpay.dispute.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DisputeMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDisputeMilestoneStore()
        val item = newDisputeMilestone("code-429", "label-173")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
