package com.acme.transport.crewing.dutyperiod.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DutyPeriodRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDutyPeriodRequestStore()
        val item = newDutyPeriodRequest("code-196", "name-158")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
