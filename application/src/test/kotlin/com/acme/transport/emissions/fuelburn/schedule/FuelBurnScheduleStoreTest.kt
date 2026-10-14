package com.acme.transport.emissions.fuelburn.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelBurnScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelBurnScheduleStore()
        val item = newFuelBurnSchedule("code-254", "label-294")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
