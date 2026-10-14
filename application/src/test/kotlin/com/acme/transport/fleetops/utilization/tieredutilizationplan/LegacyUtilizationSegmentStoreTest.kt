package com.acme.transport.fleetops.utilization.tieredutilizationplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyUtilizationSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyUtilizationSegmentStore()
        val item = newLegacyUtilizationSegment("code-29", "name-195")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
