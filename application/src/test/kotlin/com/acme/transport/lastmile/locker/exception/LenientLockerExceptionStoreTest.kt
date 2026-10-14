package com.acme.transport.lastmile.locker.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientLockerExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientLockerExceptionStore()
        val item = newLenientLockerException("code-311", "label-461")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
