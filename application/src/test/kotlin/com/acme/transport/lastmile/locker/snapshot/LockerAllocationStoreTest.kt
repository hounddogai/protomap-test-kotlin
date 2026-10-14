package com.acme.transport.lastmile.locker.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LockerAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLockerAllocationStore()
        val item = newLockerAllocation("code-861", "name-498")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
