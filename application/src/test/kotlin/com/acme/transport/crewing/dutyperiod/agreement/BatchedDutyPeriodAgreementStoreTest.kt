package com.acme.transport.crewing.dutyperiod.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedDutyPeriodAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedDutyPeriodAgreementStore()
        val item = newBatchedDutyPeriodAgreement("code-358", "title-999")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
