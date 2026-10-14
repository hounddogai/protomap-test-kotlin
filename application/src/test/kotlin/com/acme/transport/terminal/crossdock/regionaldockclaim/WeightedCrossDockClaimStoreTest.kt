package com.acme.transport.terminal.crossdock.regionaldockclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedCrossDockClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedCrossDockClaimStore()
        val item = newWeightedCrossDockClaim("code-564", "title-418")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
