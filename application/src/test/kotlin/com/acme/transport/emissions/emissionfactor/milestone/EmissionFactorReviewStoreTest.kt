package com.acme.transport.emissions.emissionfactor.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EmissionFactorReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEmissionFactorReviewStore()
        val item = newEmissionFactorReview("code-36", "name-885")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
