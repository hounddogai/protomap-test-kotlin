package com.acme.transport.fleetops.tire.tiremanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedTireMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedTireMilestoneStore()
        val item = newWeightedTireMilestone("code-251", "name-658")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
