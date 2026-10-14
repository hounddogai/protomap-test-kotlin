package com.acme.transport.lastmile.proofofdelivery.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientProofOfDeliveryRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientProofOfDeliveryRuleStore()
        val item = newLenientProofOfDeliveryRule("code-399", "label-324")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
