package com.acme.transport.lastmile.locker.lockertier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledLockerTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledLockerTierStore()
        val item = newScheduledLockerTier("code-981", "title-609")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
