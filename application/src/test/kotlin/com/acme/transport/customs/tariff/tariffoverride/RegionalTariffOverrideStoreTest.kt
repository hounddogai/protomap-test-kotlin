package com.acme.transport.customs.tariff.tariffoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalTariffOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalTariffOverrideStore()
        val item = newRegionalTariffOverride("code-71", "name-63")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
