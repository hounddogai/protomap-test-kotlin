package com.acme.transport.fleetops.serviceplan.planledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultServicePlanLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultServicePlanLedgerStore()
        val item = newDefaultServicePlanLedger("code-802", "label-248")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
