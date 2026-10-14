package com.acme.transport.crewing.dutyperiod.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DutyPeriodWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDutyPeriodWindowStore()
        val item = newDutyPeriodWindow("code-532", "label-736")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
