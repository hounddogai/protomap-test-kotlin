package com.acme.transport.network.transittime.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultTransitTimePolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultTransitTimePolicyStore()
        val item = newDefaultTransitTimePolicy("code-125", "label-482")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
