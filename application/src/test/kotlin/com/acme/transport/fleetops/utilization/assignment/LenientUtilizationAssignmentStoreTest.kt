package com.acme.transport.fleetops.utilization.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientUtilizationAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientUtilizationAssignmentStore()
        val item = newLenientUtilizationAssignment("code-931", "label-187")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
