package com.acme.transport.lastmile.proofofdelivery.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledProofOfDeliveryDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledProofOfDeliveryDigestStore()
        val item = newScheduledProofOfDeliveryDigest("code-957", "title-618")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
