package com.acme.transport.lastmile.redelivery.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RedeliveryGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRedeliveryGroupStore()
        val item = newRedeliveryGroup("code-355", "name-175")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
