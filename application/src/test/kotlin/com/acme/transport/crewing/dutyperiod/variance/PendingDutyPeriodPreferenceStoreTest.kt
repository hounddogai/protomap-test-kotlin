package com.acme.transport.crewing.dutyperiod.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingDutyPeriodPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingDutyPeriodPreferenceStore()
        val item = newPendingDutyPeriodPreference("code-20", "label-162")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
