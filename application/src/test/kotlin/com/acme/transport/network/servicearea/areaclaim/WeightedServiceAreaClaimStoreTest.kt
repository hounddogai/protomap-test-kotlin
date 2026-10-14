package com.acme.transport.network.servicearea.areaclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedServiceAreaClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedServiceAreaClaimStore()
        val item = newWeightedServiceAreaClaim("code-89", "name-356")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
