package com.acme.transport.fleetops.serviceplan.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalServicePlanBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalServicePlanBaselineStore()
        val item = newSeasonalServicePlanBaseline("code-500", "label-54")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
