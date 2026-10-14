package com.acme.transport.customs.tariff.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalTariffOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalTariffOverrideStore()
        val item = newSeasonalTariffOverride("code-903", "name-823")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
