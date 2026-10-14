package com.acme.transport.fleetops.utilization.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class UtilizationBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryUtilizationBaselineStore()
        val item = newUtilizationBaseline("code-976", "name-435")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
