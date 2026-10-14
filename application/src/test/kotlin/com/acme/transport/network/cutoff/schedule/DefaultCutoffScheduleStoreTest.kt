package com.acme.transport.network.cutoff.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultCutoffScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultCutoffScheduleStore()
        val item = newDefaultCutoffSchedule("code-773", "label-511")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
