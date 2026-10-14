package com.acme.transport.lastmile.locker.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveLockerClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveLockerClaimStore()
        val item = newAdaptiveLockerClaim("code-999", "label-914")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
