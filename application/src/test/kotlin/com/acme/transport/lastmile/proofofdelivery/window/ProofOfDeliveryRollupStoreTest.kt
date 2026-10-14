package com.acme.transport.lastmile.proofofdelivery.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ProofOfDeliveryRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryProofOfDeliveryRollupStore()
        val item = newProofOfDeliveryRollup("code-556", "title-57")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
