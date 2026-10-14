package com.acme.transport.lastmile.redelivery.redeliveryentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RedeliveryProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRedeliveryProfileStore()
        val item = newRedeliveryProfile("code-273", "name-26")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
