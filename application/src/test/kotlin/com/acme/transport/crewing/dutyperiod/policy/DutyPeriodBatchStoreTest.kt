package com.acme.transport.crewing.dutyperiod.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DutyPeriodBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDutyPeriodBatchStore()
        val item = newDutyPeriodBatch("code-60", "title-275")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
