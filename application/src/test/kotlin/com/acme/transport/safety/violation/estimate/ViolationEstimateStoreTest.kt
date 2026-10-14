package com.acme.transport.safety.violation.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ViolationEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryViolationEstimateStore()
        val item = newViolationEstimate("code-213", "name-879")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
