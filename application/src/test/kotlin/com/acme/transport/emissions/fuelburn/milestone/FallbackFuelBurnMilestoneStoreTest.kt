package com.acme.transport.emissions.fuelburn.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackFuelBurnMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackFuelBurnMilestoneStore()
        val item = newFallbackFuelBurnMilestone("code-562", "title-382")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
