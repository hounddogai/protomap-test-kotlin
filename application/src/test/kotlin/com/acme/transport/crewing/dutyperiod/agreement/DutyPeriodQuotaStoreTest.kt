package com.acme.transport.crewing.dutyperiod.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DutyPeriodQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDutyPeriodQuotaStore()
        val item = newDutyPeriodQuota("code-542", "title-460")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
