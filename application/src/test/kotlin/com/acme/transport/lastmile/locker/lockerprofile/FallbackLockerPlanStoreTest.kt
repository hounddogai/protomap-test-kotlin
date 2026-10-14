package com.acme.transport.lastmile.locker.lockerprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackLockerPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackLockerPlanStore()
        val item = newFallbackLockerPlan("code-724", "title-742")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
