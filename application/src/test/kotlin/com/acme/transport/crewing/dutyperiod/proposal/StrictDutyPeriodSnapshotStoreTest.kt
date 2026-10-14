package com.acme.transport.crewing.dutyperiod.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictDutyPeriodSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictDutyPeriodSnapshotStore()
        val item = newStrictDutyPeriodSnapshot("code-90", "name-802")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
