package com.acme.transport.lastmile.parcel.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ParcelScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryParcelScheduleStore()
        val item = newParcelSchedule("code-390", "title-13")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
