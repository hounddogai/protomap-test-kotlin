package com.acme.transport.emissions.emissionfactor.factortarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveEmissionFactorMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveEmissionFactorMilestoneStore()
        val item = newAdaptiveEmissionFactorMilestone("code-274", "title-743")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
