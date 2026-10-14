package com.acme.transport.lastmile.redelivery.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RedeliveryScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRedeliveryScoreStore()
        val item = newRedeliveryScore("code-922", "title-184")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
