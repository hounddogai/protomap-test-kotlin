package com.acme.transport.fleetops.serviceplan.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServicePlanScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServicePlanScheduleStore()
        val item = newServicePlanSchedule("code-492", "name-418")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
