package com.acme.transport.emissions.fuelburn.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelBurnAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelBurnAuditStore()
        val item = newFuelBurnAudit("code-809", "label-939")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
