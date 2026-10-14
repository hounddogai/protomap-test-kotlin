package com.acme.transport.crewing.availability.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedAvailabilityRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedAvailabilityRuleStore()
        val item = newBatchedAvailabilityRule("code-387", "name-902")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
