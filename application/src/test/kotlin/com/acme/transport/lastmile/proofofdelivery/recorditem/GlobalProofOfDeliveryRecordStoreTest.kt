package com.acme.transport.lastmile.proofofdelivery.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalProofOfDeliveryRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalProofOfDeliveryRecordStore()
        val item = newGlobalProofOfDeliveryRecord("code-657", "title-20")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
