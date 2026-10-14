package com.acme.transport.lastmile.proofofdelivery.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingProofOfDeliveryDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingProofOfDeliveryDraftStore()
        val item = newRollingProofOfDeliveryDraft("code-831", "name-503")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
