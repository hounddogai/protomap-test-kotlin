package com.acme.transport.fleetops.serviceplan.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalServicePlanPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalServicePlanPreferenceStore()
        val item = newSeasonalServicePlanPreference("code-77", "label-221")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
