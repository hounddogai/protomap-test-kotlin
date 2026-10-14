package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingUtilizationScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingUtilizationScoreStore()
        val item = newRollingUtilizationScore("code-692", "name-655")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
