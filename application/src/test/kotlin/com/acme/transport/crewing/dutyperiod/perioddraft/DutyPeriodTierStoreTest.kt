package com.acme.transport.crewing.dutyperiod.perioddraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DutyPeriodTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDutyPeriodTierStore()
        val item = newDutyPeriodTier("code-170", "label-368")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
