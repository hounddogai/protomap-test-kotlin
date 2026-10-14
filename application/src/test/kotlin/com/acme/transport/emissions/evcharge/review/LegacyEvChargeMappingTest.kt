package com.acme.transport.emissions.evcharge.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyEvChargeMappingTest {
    @Test
    fun `starts in idle`() {
        val item = newLegacyEvChargeMapping("code-777", "name-851")
        assertEquals(LegacyEvChargeMappingStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newLegacyEvChargeMapping("code-777", "name-851")
        try {
            item.moveTo(LegacyEvChargeMappingStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyEvChargeMappingStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLegacyEvChargeMapping("code-777", "name-851")
        item.status = LegacyEvChargeMappingStatus.STOPPED
        assertFails("move") { item.moveTo(LegacyEvChargeMappingStatus.IDLE) }
    }
}
