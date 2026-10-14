package com.acme.transport.emissions.fuelburn.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryFuelBurnReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryFuelBurnReviewStore()
        val item = newPrimaryFuelBurnReview("code-533", "title-118")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
