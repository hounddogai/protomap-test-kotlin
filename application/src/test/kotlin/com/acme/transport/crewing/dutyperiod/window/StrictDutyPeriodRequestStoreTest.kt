package com.acme.transport.crewing.dutyperiod.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictDutyPeriodRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictDutyPeriodRequestStore()
        val item = newStrictDutyPeriodRequest("code-579", "name-980")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
