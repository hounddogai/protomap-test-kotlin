package com.acme.transport.crewing.dutyperiod.periodthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualDutyPeriodExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualDutyPeriodExceptionStore()
        val item = newManualDutyPeriodException("code-710", "title-180")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
