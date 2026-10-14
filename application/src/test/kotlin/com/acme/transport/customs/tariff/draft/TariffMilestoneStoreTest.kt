package com.acme.transport.customs.tariff.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TariffMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTariffMilestoneStore()
        val item = newTariffMilestone("code-428", "name-498")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
