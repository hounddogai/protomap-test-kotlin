package com.acme.transport.lastmile.redelivery.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RedeliveryTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRedeliveryTargetStore()
        val item = newRedeliveryTarget("code-716", "name-380")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
