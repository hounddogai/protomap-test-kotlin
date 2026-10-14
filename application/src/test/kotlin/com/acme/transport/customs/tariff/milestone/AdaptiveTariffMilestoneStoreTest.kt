package com.acme.transport.customs.tariff.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveTariffMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveTariffMilestoneStore()
        val item = newAdaptiveTariffMilestone("code-175", "label-539")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
