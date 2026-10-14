package com.acme.transport.lastmile.locker.lockerrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledLockerRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledLockerRuleStore()
        val item = newScheduledLockerRule("code-742", "label-846")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
