package com.acme.transport.customs.hold.adaptiveholdmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledHoldProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledHoldProfileStore()
        val item = newScheduledHoldProfile("code-991", "name-648")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
