package com.acme.transport.lastmile.locker.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LockerLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLockerLedgerStore()
        val item = newLockerLedger("code-122", "title-520")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
