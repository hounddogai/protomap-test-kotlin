package com.acme.transport.lastmile.redelivery.redeliveryclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RedeliveryRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRedeliveryRuleStore()
        val item = newRedeliveryRule("code-454", "title-267")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
