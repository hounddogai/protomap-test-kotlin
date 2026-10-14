package com.acme.transport.crewing.dutyperiod.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveDutyPeriodMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveDutyPeriodMilestoneStore()
        val item = newAdaptiveDutyPeriodMilestone("code-254", "title-233")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
