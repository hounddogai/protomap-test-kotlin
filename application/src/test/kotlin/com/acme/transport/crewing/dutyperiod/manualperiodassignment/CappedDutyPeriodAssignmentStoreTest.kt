package com.acme.transport.crewing.dutyperiod.manualperiodassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedDutyPeriodAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedDutyPeriodAssignmentStore()
        val item = newCappedDutyPeriodAssignment("code-886", "title-884")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
