package com.acme.transport.crewing.dutyperiod.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingDutyPeriodPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingDutyPeriodPolicyStore()
        val item = newPendingDutyPeriodPolicy("code-681", "title-979")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
