package com.acme.transport.safety.violation.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalViolationAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalViolationAllocationStore()
        val item = newSeasonalViolationAllocation("code-2", "label-645")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
