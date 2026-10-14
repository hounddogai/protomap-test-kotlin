package com.acme.transport.crewing.dutyperiod.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DutyPeriodRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDutyPeriodRecordStore()
        val item = newDutyPeriodRecord("code-704", "name-204")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
