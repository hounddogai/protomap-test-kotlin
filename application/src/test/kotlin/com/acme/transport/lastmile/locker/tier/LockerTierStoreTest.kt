package com.acme.transport.lastmile.locker.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LockerTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLockerTierStore()
        val item = newLockerTier("code-236", "title-529")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
