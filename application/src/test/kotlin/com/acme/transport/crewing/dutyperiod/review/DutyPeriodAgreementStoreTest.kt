package com.acme.transport.crewing.dutyperiod.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DutyPeriodAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDutyPeriodAgreementStore()
        val item = newDutyPeriodAgreement("code-983", "label-493")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
