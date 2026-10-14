package com.acme.transport.lastmile.redelivery.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalRedeliveryClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalRedeliveryClaimStore()
        val item = newRegionalRedeliveryClaim("code-235", "label-387")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
