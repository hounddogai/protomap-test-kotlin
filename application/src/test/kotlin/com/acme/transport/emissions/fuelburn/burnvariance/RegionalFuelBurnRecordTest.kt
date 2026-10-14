package com.acme.transport.emissions.fuelburn.burnvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalFuelBurnRecordTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalFuelBurnRecord("code-898", "title-846")
        assertEquals(RegionalFuelBurnRecordStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRegionalFuelBurnRecord("code-898", "title-846")
        try {
            item.moveTo(RegionalFuelBurnRecordStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalFuelBurnRecordStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalFuelBurnRecord("code-898", "title-846")
        item.status = RegionalFuelBurnRecordStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalFuelBurnRecordStatus.CREATED) }
    }
}
