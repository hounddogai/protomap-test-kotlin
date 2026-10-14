package com.acme.transport.fleetops.serviceplan.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalServicePlanRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalServicePlanRecordStore()
        val item = newSeasonalServicePlanRecord("code-214", "name-159")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
