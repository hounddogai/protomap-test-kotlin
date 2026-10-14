package com.acme.transport.emissions.fuelburn.burnwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingFuelBurnAgreementTest {
    @Test
    fun `starts in planned`() {
        val item = newRollingFuelBurnAgreement("code-557", "label-853")
        assertEquals(RollingFuelBurnAgreementStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newRollingFuelBurnAgreement("code-557", "label-853")
        try {
            item.moveTo(RollingFuelBurnAgreementStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingFuelBurnAgreementStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRollingFuelBurnAgreement("code-557", "label-853")
        item.status = RollingFuelBurnAgreementStatus.CANCELLED
        assertFails("move") { item.moveTo(RollingFuelBurnAgreementStatus.PLANNED) }
    }
}
