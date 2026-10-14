package com.acme.transport.lastmile.drop.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DropExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDropExceptionStore()
        val item = newDropException("code-689", "name-947")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
