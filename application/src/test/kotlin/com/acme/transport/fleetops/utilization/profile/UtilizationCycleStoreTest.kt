package com.acme.transport.fleetops.utilization.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class UtilizationCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryUtilizationCycleStore()
        val item = newUtilizationCycle("code-527", "label-687")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
