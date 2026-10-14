package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingProofOfDeliveryEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingProofOfDeliveryEntryStore()
        val item = newRollingProofOfDeliveryEntry("code-578", "title-896")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
