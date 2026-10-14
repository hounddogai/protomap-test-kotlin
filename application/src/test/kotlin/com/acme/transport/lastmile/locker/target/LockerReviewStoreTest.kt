package com.acme.transport.lastmile.locker.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LockerReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLockerReviewStore()
        val item = newLockerReview("code-955", "label-117")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
