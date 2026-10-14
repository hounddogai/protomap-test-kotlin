package com.acme.transport.emissions.fuelburn.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultFuelBurnAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultFuelBurnAssignmentStore()
        val item = newDefaultFuelBurnAssignment("code-222", "title-900")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
