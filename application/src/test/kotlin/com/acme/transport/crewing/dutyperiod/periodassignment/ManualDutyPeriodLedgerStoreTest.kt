package com.acme.transport.crewing.dutyperiod.periodassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualDutyPeriodLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualDutyPeriodLedgerStore()
        val item = newManualDutyPeriodLedger("code-696", "label-975")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
