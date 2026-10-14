package com.acme.transport.fleetops.utilization.utilizationestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class UtilizationRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryUtilizationRequestStore()
        val item = newUtilizationRequest("code-931", "label-952")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
