package com.acme.transport.kit.money

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class MoneyTest {
    @Test
    fun `splits without losing a cent`() {
        val total = of(1001, "usd")
        val shares = total.split(3)
        assertEquals(3, shares.size, "shares")
        assertEquals(334, shares[0].units, "first share")
        assertEquals("USD", total.currency, "currency")
    }

    @Test
    fun `refuses to mix currencies`() {
        val euros = of(100, "EUR")
        assertFails("adding dollars to euros") { euros.plus(of(5, "USD")) }
    }
}
