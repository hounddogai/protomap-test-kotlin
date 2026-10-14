package com.acme.transport.emissions.emissionfactor.factormilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingEmissionFactorOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingEmissionFactorOverrideStore()
        val item = newPendingEmissionFactorOverride("code-970", "name-531")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
