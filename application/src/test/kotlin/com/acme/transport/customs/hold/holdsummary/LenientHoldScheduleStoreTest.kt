package com.acme.transport.customs.hold.holdsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientHoldScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientHoldScheduleStore()
        val item = newLenientHoldSchedule("code-344", "name-518")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
