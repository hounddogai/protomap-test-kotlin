package com.acme.transport.lastmile.redelivery.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RedeliveryMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRedeliveryMilestoneStore()
        val item = newRedeliveryMilestone("code-739", "name-982")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
