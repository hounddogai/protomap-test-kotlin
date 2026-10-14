package com.acme.transport.lastmile.locker.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultLockerPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultLockerPlanStore()
        val item = newDefaultLockerPlan("code-574", "title-711")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
