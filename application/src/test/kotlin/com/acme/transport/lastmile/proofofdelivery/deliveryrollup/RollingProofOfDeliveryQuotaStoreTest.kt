package com.acme.transport.lastmile.proofofdelivery.deliveryrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingProofOfDeliveryQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingProofOfDeliveryQuotaStore()
        val item = newRollingProofOfDeliveryQuota("code-460", "title-532")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
