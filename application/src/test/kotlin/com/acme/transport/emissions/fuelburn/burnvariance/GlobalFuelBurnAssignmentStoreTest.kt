package com.acme.transport.emissions.fuelburn.burnvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalFuelBurnAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalFuelBurnAssignmentStore()
        val item = newGlobalFuelBurnAssignment("code-822", "name-881")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
