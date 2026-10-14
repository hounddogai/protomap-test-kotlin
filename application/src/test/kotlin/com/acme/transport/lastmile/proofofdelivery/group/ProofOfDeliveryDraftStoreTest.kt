package com.acme.transport.lastmile.proofofdelivery.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ProofOfDeliveryDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryProofOfDeliveryDraftStore()
        val item = newProofOfDeliveryDraft("code-848", "title-561")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
