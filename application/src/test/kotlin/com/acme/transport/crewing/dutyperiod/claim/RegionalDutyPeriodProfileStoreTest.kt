package com.acme.transport.crewing.dutyperiod.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalDutyPeriodProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalDutyPeriodProfileStore()
        val item = newRegionalDutyPeriodProfile("code-164", "name-619")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
