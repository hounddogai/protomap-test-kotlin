package com.acme.transport.crewing.dutyperiod.periodproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalDutyPeriodTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalDutyPeriodTierStore()
        val item = newRegionalDutyPeriodTier("code-211", "name-555")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
