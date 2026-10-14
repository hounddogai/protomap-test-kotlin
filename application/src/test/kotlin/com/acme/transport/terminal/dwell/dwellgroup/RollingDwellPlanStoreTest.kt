package com.acme.transport.terminal.dwell.dwellgroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingDwellPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingDwellPlanStore()
        val item = newRollingDwellPlan("code-620", "name-454")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
