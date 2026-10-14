package com.acme.transport.freightpay.accessorial.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientAccessorialReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientAccessorialReviewStore()
        val item = newLenientAccessorialReview("code-584", "name-434")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
