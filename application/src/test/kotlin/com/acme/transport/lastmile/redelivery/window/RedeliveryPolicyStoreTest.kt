package com.acme.transport.lastmile.redelivery.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RedeliveryPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRedeliveryPolicyStore()
        val item = newRedeliveryPolicy("code-632", "title-166")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
