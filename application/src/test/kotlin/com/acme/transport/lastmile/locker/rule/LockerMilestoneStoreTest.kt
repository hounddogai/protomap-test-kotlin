package com.acme.transport.lastmile.locker.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LockerMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLockerMilestoneStore()
        val item = newLockerMilestone("code-172", "title-200")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
