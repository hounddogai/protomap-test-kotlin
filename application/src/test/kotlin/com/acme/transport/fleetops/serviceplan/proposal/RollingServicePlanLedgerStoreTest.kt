package com.acme.transport.fleetops.serviceplan.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingServicePlanLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingServicePlanLedgerStore()
        val item = newRollingServicePlanLedger("code-45", "name-698")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
