package com.acme.transport.crewing.dutyperiod.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DutyPeriodLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDutyPeriodLedgerStore()
        val item = newDutyPeriodLedger("code-343", "name-635")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
