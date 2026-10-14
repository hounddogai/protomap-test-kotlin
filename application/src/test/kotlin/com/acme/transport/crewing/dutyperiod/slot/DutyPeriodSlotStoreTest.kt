package com.acme.transport.crewing.dutyperiod.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DutyPeriodSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDutyPeriodSlotStore()
        val item = newDutyPeriodSlot("code-352", "name-923")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
