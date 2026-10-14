package com.acme.transport.lastmile.redelivery.redeliveryrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledRedeliveryRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledRedeliveryRuleStore()
        val item = newScheduledRedeliveryRule("code-157", "name-569")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
