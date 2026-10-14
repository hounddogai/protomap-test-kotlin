package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultUtilizationPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultUtilizationPolicyStore()
        val item = newDefaultUtilizationPolicy("code-274", "title-608")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
