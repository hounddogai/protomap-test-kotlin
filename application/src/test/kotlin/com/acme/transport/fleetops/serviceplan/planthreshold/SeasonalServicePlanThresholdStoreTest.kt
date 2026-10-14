package com.acme.transport.fleetops.serviceplan.planthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalServicePlanThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalServicePlanThresholdStore()
        val item = newSeasonalServicePlanThreshold("code-169", "label-790")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
