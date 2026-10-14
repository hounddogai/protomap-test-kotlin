package com.acme.transport.freightpay.fuelsurcharge.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingFuelSurchargePreferenceTest {
    @Test
    fun `starts in open`() {
        val item = newRollingFuelSurchargePreference("code-915", "title-563")
        assertEquals(RollingFuelSurchargePreferenceStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newRollingFuelSurchargePreference("code-915", "title-563")
        try {
            item.moveTo(RollingFuelSurchargePreferenceStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingFuelSurchargePreferenceStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRollingFuelSurchargePreference("code-915", "title-563")
        item.status = RollingFuelSurchargePreferenceStatus.ARCHIVED
        assertFails("move") { item.moveTo(RollingFuelSurchargePreferenceStatus.OPEN) }
    }
}
