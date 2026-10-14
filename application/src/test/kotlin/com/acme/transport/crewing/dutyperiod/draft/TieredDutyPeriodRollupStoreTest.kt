package com.acme.transport.crewing.dutyperiod.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredDutyPeriodRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredDutyPeriodRollupStore()
        val item = newTieredDutyPeriodRollup("code-531", "title-277")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
