package com.acme.transport.crewing.dutyperiod.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DutyPeriodGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDutyPeriodGroupStore()
        val item = newDutyPeriodGroup("code-983", "title-636")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
