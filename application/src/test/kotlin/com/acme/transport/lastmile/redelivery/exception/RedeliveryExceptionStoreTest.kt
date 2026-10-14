package com.acme.transport.lastmile.redelivery.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RedeliveryExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRedeliveryExceptionStore()
        val item = newRedeliveryException("code-343", "title-713")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
