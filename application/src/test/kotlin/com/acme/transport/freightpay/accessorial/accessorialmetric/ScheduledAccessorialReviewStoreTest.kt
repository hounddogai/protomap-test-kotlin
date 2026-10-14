package com.acme.transport.freightpay.accessorial.accessorialmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledAccessorialReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledAccessorialReviewStore()
        val item = newScheduledAccessorialReview("code-511", "label-460")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
