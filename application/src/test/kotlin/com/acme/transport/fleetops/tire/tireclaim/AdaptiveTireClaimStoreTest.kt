package com.acme.transport.fleetops.tire.tireclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveTireClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveTireClaimStore()
        val item = newAdaptiveTireClaim("code-848", "title-794")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
