package com.acme.transport.lastmile.proofofdelivery.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ProofOfDeliveryAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryProofOfDeliveryAssignmentStore()
        val item = newProofOfDeliveryAssignment("code-361", "label-199")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
