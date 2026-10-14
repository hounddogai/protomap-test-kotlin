package com.acme.transport.emissions.fuelburn.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelBurnClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelBurnClaimStore()
        val item = newFuelBurnClaim("code-291", "title-204")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
