package com.acme.transport.fleetops.serviceplan.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackServicePlanLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackServicePlanLedgerStore()
        val item = newFallbackServicePlanLedger("code-941", "title-50")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
