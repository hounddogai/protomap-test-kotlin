package com.acme.transport.lastmile.proofofdelivery.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingProofOfDeliveryCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingProofOfDeliveryCycleStore()
        val item = newRollingProofOfDeliveryCycle("code-862", "title-700")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
