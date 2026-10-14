package com.acme.transport.lastmile.locker.lockertarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledLockerReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledLockerReviewStore()
        val item = newScheduledLockerReview("code-729", "name-104")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
