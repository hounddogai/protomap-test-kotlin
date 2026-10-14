package com.acme.transport.fleetops.utilization.utilizationsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalUtilizationTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalUtilizationTargetStore()
        val item = newSeasonalUtilizationTarget("code-404", "label-12")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
