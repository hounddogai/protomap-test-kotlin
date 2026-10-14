package com.acme.transport.fleetops.utilization.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyUtilizationSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyUtilizationSlotStore()
        val item = newLegacyUtilizationSlot("code-556", "label-415")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
