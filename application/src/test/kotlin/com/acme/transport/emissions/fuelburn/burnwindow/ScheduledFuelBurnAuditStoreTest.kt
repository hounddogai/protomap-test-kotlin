package com.acme.transport.emissions.fuelburn.burnwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledFuelBurnAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledFuelBurnAuditStore()
        val item = newScheduledFuelBurnAudit("code-424", "label-81")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
