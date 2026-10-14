package com.acme.transport.lastmile.locker.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LockerExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLockerExceptionStore()
        val item = newLockerException("code-479", "name-401")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
