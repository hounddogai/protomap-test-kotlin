package com.acme.transport.freightpay.fuelsurcharge.surchargerule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredFuelSurchargeRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredFuelSurchargeRuleStore()
        val item = newTieredFuelSurchargeRule("code-715", "title-410")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
