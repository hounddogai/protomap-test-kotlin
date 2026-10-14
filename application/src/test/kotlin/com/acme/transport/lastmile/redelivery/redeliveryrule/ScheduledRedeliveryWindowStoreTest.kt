package com.acme.transport.lastmile.redelivery.redeliveryrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledRedeliveryWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledRedeliveryWindowStore()
        val item = newScheduledRedeliveryWindow("code-717", "name-554")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
