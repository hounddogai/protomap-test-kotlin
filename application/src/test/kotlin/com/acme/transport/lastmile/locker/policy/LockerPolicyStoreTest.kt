package com.acme.transport.lastmile.locker.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LockerPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLockerPolicyStore()
        val item = newLockerPolicy("code-77", "title-108")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
