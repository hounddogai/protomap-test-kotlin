package com.acme.transport.crewing.dutyperiod.periodsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DutyPeriodMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDutyPeriodMilestoneStore()
        val item = newDutyPeriodMilestone("code-460", "title-860")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
