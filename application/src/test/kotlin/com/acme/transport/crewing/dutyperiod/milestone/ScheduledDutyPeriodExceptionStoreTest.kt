package com.acme.transport.crewing.dutyperiod.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledDutyPeriodExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledDutyPeriodExceptionStore()
        val item = newScheduledDutyPeriodException("code-333", "name-470")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
