package com.acme.transport.lastmile.redelivery.redeliveryclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryRedeliveryClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryRedeliveryClaimStore()
        val item = newPrimaryRedeliveryClaim("code-85", "label-204")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
