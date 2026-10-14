package com.acme.transport.crewing.dutyperiod.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingDutyPeriodWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingDutyPeriodWindowStore()
        val item = newRollingDutyPeriodWindow("code-541", "name-629")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
