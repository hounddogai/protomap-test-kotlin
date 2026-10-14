package com.acme.transport.crewing.dutyperiod.periodnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualDutyPeriodNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualDutyPeriodNoticeStore()
        val item = newManualDutyPeriodNotice("code-147", "label-252")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
