package com.acme.transport.fleetops.utilization.utilizationrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalUtilizationPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalUtilizationPreferenceStore()
        val item = newSeasonalUtilizationPreference("code-660", "name-468")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
