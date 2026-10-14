package com.acme.transport.safety.violation.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ViolationMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryViolationMilestoneStore()
        val item = newViolationMilestone("code-488", "label-912")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
