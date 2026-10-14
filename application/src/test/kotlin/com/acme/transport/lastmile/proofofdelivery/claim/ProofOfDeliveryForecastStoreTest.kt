package com.acme.transport.lastmile.proofofdelivery.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ProofOfDeliveryForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryProofOfDeliveryForecastStore()
        val item = newProofOfDeliveryForecast("code-704", "title-793")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
