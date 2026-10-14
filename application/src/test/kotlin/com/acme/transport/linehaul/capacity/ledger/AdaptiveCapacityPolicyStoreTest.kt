package com.acme.transport.linehaul.capacity.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveCapacityPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveCapacityPolicyStore()
        val item = newAdaptiveCapacityPolicy("code-603", "name-182")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
