package com.acme.transport.lastmile.proofofdelivery.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredProofOfDeliveryScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredProofOfDeliveryScoreStore()
        val item = newTieredProofOfDeliveryScore("code-223", "name-11")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
