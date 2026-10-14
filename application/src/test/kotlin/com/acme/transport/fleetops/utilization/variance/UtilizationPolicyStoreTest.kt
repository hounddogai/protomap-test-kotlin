package com.acme.transport.fleetops.utilization.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class UtilizationPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryUtilizationPolicyStore()
        val item = newUtilizationPolicy("code-978", "title-719")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
