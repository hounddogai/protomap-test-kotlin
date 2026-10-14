package com.acme.transport.crewing.dutyperiod.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyDutyPeriodMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyDutyPeriodMappingStore()
        val item = newLegacyDutyPeriodMapping("code-122", "title-10")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
