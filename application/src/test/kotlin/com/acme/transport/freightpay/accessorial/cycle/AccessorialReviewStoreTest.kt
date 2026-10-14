package com.acme.transport.freightpay.accessorial.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AccessorialReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAccessorialReviewStore()
        val item = newAccessorialReview("code-120", "title-727")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
