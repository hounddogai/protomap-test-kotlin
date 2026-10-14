package com.acme.transport.lastmile.redelivery.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalRedeliveryScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalRedeliveryScheduleStore()
        val item = newRegionalRedeliverySchedule("code-654", "label-871")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
