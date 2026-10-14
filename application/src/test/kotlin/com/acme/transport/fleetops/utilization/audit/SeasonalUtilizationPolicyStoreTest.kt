package com.acme.transport.fleetops.utilization.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalUtilizationPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalUtilizationPolicyStore()
        val item = newSeasonalUtilizationPolicy("code-918", "name-372")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
