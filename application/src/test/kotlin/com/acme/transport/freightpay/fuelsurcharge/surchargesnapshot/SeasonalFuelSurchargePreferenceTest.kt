package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalFuelSurchargePreferenceTest {
    @Test
    fun `starts in requested`() {
        val item = newSeasonalFuelSurchargePreference("code-26", "label-335")
        assertEquals(SeasonalFuelSurchargePreferenceStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newSeasonalFuelSurchargePreference("code-26", "label-335")
        try {
            item.moveTo(SeasonalFuelSurchargePreferenceStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalFuelSurchargePreferenceStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSeasonalFuelSurchargePreference("code-26", "label-335")
        item.status = SeasonalFuelSurchargePreferenceStatus.EXPIRED
        assertFails("move") { item.moveTo(SeasonalFuelSurchargePreferenceStatus.REQUESTED) }
    }
}
