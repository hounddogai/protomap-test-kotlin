package com.acme.transport.lastmile.locker.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LockerTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLockerTargetStore()
        val item = newLockerTarget("code-125", "name-130")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
