package com.acme.transport.network.servicearea.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServiceAreaScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServiceAreaScheduleStore()
        val item = newServiceAreaSchedule("code-692", "title-802")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
