package com.acme.transport.emissions.emissionfactor.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalEmissionFactorSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalEmissionFactorSlotStore()
        val item = newGlobalEmissionFactorSlot("code-41", "name-713")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
