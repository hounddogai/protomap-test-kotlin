package com.acme.transport.lastmile.locker.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingLockerPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingLockerPlanStore()
        val item = newRollingLockerPlan("code-497", "title-766")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
