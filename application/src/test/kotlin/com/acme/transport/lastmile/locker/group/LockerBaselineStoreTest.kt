package com.acme.transport.lastmile.locker.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LockerBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLockerBaselineStore()
        val item = newLockerBaseline("code-272", "title-178")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
