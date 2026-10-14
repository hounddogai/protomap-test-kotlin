package com.acme.transport.customs.tariff.fallbacktariffcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalTariffRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalTariffRuleStore()
        val item = newSeasonalTariffRule("code-455", "title-576")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
