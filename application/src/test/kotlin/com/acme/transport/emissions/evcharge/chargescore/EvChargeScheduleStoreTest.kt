package com.acme.transport.emissions.evcharge.chargescore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EvChargeScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEvChargeScheduleStore()
        val item = newEvChargeSchedule("code-405", "label-827")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
