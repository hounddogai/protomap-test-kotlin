package com.acme.transport.lastmile.locker.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedLockerCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedLockerCycleStore()
        val item = newWeightedLockerCycle("code-221", "name-735")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
