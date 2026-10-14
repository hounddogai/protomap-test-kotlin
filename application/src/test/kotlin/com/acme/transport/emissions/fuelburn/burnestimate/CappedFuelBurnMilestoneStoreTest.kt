package com.acme.transport.emissions.fuelburn.burnestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedFuelBurnMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedFuelBurnMilestoneStore()
        val item = newCappedFuelBurnMilestone("code-249", "title-598")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
