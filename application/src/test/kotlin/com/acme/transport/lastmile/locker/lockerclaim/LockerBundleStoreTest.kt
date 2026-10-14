package com.acme.transport.lastmile.locker.lockerclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LockerBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLockerBundleStore()
        val item = newLockerBundle("code-804", "name-765")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
