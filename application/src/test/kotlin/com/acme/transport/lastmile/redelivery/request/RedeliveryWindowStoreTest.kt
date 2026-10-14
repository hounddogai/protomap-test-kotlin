package com.acme.transport.lastmile.redelivery.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RedeliveryWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRedeliveryWindowStore()
        val item = newRedeliveryWindow("code-22", "label-155")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
