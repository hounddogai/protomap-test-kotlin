package com.acme.transport.freightpay.fuelsurcharge.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingFuelSurchargePreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingFuelSurchargePreferenceStore()
        val item = newRollingFuelSurchargePreference("code-915", "title-563")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
