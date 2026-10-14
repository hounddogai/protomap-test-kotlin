package com.acme.transport.crewing.dutyperiod.globalperiodsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveDutyPeriodCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveDutyPeriodCheckpointStore()
        val item = newAdaptiveDutyPeriodCheckpoint("code-860", "title-576")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
