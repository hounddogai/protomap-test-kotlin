package com.acme.transport.fleetops.serviceplan.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualServicePlanSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualServicePlanSlotStore()
        val item = newManualServicePlanSlot("code-326", "label-398")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
