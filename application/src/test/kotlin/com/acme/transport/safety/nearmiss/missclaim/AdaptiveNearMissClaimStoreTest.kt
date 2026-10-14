package com.acme.transport.safety.nearmiss.missclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveNearMissClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveNearMissClaimStore()
        val item = newAdaptiveNearMissClaim("code-459", "name-427")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
