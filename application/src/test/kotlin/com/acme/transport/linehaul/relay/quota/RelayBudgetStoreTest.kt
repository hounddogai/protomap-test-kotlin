package com.acme.transport.linehaul.relay.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RelayBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRelayBudgetStore()
        val item = newRelayBudget("code-809", "label-310")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
