package com.acme.transport.crewing.dutyperiod.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DutyPeriodSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDutyPeriodSnapshotStore()
        val item = newDutyPeriodSnapshot("code-200", "title-914")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
