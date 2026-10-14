package com.acme.transport.linehaul.laneplan.planagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedLanePlanAgreementTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedLanePlanAgreement("code-318", "label-806")
        assertEquals(WeightedLanePlanAgreementStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newWeightedLanePlanAgreement("code-318", "label-806")
        try {
            item.moveTo(WeightedLanePlanAgreementStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedLanePlanAgreementStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedLanePlanAgreement("code-318", "label-806")
        item.status = WeightedLanePlanAgreementStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedLanePlanAgreementStatus.PLANNED) }
    }
}
