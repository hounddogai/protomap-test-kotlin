package com.acme.transport.linehaul.backhaul.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingBackhaulReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingBackhaulReviewStore()
        val item = newRollingBackhaulReview("code-35", "name-492")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
